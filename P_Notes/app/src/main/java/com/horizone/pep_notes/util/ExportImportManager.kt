package com.horizone.pep_notes.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import com.horizone.pep_notes.data.db.PepDatabase
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import com.horizone.pep_notes.util.normalizeColor
import com.horizone.pep_notes.util.normalizeLabelName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ImportErrorType {
    PARSE_ERROR,
    VALIDATION_ERROR,
    INTERNAL_ERROR
}

enum class IoErrorType {
    FILE_NOT_FOUND,
    PERMISSION_DENIED,
    READ_ERROR,
    WRITE_ERROR,
    NO_SPACE,
    FILESYSTEM_UNAVAILABLE,
    INVALID_URI,
    UNKNOWN
}

enum class ImportMode {
    Replace,
    Merge
}

sealed class FileReadResult {
    data class Success(val data: String) : FileReadResult()
    data class Error(val error: IoErrorType, val throwable: Throwable? = null) : FileReadResult()
}

sealed class FileWriteResult {
    data class Success(val uri: Uri? = null, val file: File? = null) : FileWriteResult()
    data class Error(val error: IoErrorType, val throwable: Throwable? = null) : FileWriteResult()
}

data class ImportResult(
    val success: Boolean,
    val message: String,
    val errorType: ImportErrorType? = null
)

class ExportImportManager(private val context: Context, private val database: PepDatabase) {
    private val gson = Gson()

    companion object {
        private const val MIN_SCHEMA_VERSION = 1
        private const val MAX_SCHEMA_VERSION = 1
        private const val APP_ID = "com.horizone.pep_notes"
    }

    suspend fun exportData(): String = withContext(Dispatchers.IO) {
        val jsonObject = JsonObject()

        try {
            // Add backup metadata
            val appVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }

            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

            jsonObject.addProperty("appId", context.packageName)
            jsonObject.addProperty("schemaVersion", MAX_SCHEMA_VERSION)
            jsonObject.addProperty("appVersion", appVersion)
            jsonObject.addProperty("createdAt", createdAt)

            // Get all data from database
            val persons = database.personDao().getAllPersons().first()
            val personLabels = database.personLabelDao().getAllLabels().first()
            val notes = database.noteDao().getNotesForPerson(0).first()
            val noteLabels = database.noteLabelDao().getAllLabels().first()

            // Get all notes (not just for person 0)
            val allNotes = mutableListOf<Note>()
            for (person in persons) {
                allNotes.addAll(database.noteDao().getNotesForPerson(person.id).first())
            }

            // Get cross references
            val personLabelCrossRefs = database.personLabelDao().getAllPersonLabelCrossRefsOnce()
            val noteLabelCrossRefs = database.noteLabelDao().getAllNoteLabelCrossRefsOnce()
            
            // Convert to JSON
            jsonObject.add("persons", gson.toJsonTree(persons))
            jsonObject.add("personLabels", gson.toJsonTree(personLabels))
            jsonObject.add("notes", gson.toJsonTree(allNotes))
            jsonObject.add("noteLabels", gson.toJsonTree(noteLabels))
            jsonObject.add("personLabelCrossRefs", gson.toJsonTree(personLabelCrossRefs))
            jsonObject.add("noteLabelCrossRefs", gson.toJsonTree(noteLabelCrossRefs))

            gson.toJson(jsonObject)
        } catch (e: Exception) {
            "{\"error\": \"${e.message}\"}"
        }
    }

    suspend fun importData(jsonString: String, mode: ImportMode = ImportMode.Replace): ImportResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonObject = try {
                gson.fromJson(jsonString, JsonObject::class.java)
            } catch (e: Exception) {
                return@withContext ImportResult(
                    success = false,
                    message = "✗ Import failed: Invalid JSON backup file (cannot parse).",
                    errorType = ImportErrorType.PARSE_ERROR
                )
            }

            val hasAppId = jsonObject.has("appId")
            val hasSchemaVersion = jsonObject.has("schemaVersion")
            if (hasAppId || hasSchemaVersion) {
                val appIdEl = jsonObject.get("appId")
                val schemaEl = jsonObject.get("schemaVersion")
                if (!hasAppId || appIdEl == null || !appIdEl.isJsonPrimitive) {
                    return@withContext ImportResult(
                        success = false,
                        message = "✗ Import failed: Invalid backup metadata (appId).",
                        errorType = ImportErrorType.VALIDATION_ERROR
                    )
                }
                if (!hasSchemaVersion || schemaEl == null || !schemaEl.isJsonPrimitive || !schemaEl.asJsonPrimitive.isNumber) {
                    return@withContext ImportResult(
                        success = false,
                        message = "✗ Import failed: Invalid backup metadata (schemaVersion).",
                        errorType = ImportErrorType.VALIDATION_ERROR
                    )
                }
                val appId = appIdEl.asString
                val schemaVersion = schemaEl.asInt
                val appIdMatches = appId == context.packageName || appId == APP_ID
                val schemaInRange = schemaVersion in MIN_SCHEMA_VERSION..MAX_SCHEMA_VERSION
                if (!appIdMatches || !schemaInRange) {
                    return@withContext ImportResult(
                        success = false,
                        message = "✗ Import failed: Backup is for a different app or unsupported version.",
                        errorType = ImportErrorType.VALIDATION_ERROR
                    )
                }
            }

            val keysShouldBeArrays = listOf(
                "persons",
                "personLabels",
                "notes",
                "noteLabels",
                "personLabelCrossRefs",
                "noteLabelCrossRefs"
            )
            for (k in keysShouldBeArrays) {
                if (jsonObject.has(k) && !jsonObject.get(k).isJsonArray) {
                    return@withContext ImportResult(
                        success = false,
                        message = "✗ Import failed: Invalid backup structure ($k).",
                        errorType = ImportErrorType.VALIDATION_ERROR
                    )
                }
            }

            // Parse arrays (may be null in very old backups)
            val personsArray: JsonArray? = jsonObject.getAsJsonArray("persons")
            val personLabelsArray: JsonArray? = jsonObject.getAsJsonArray("personLabels")
            val notesArray: JsonArray? = jsonObject.getAsJsonArray("notes")
            val noteLabelsArray: JsonArray? = jsonObject.getAsJsonArray("noteLabels")
            val personLabelCrossRefsArray: JsonArray? = jsonObject.getAsJsonArray("personLabelCrossRefs")
            val noteLabelCrossRefsArray: JsonArray? = jsonObject.getAsJsonArray("noteLabelCrossRefs")

            // DAO references
            val personDao = database.personDao()
            val personLabelDao = database.personLabelDao()
            val noteDao = database.noteDao()
            val noteLabelDao = database.noteLabelDao()

            // ID remapping to avoid primary-key collisions and keep foreign keys consistent
            val personIdMap = mutableMapOf<Int, Int>()
            val personLabelIdMap = mutableMapOf<Int, Int>()
            val noteIdMap = mutableMapOf<Int, Int>()
            val noteLabelIdMap = mutableMapOf<Int, Int>()

            data class LabelKey(val name: String, val color: String)
            val personLabelKeyMap = mutableMapOf<LabelKey, Int>()
            val noteLabelKeyMap = mutableMapOf<LabelKey, Int>()
            val canonicalPersonMap = mutableMapOf<LabelKey, Int>()
            val canonicalNoteMap = mutableMapOf<LabelKey, Int>()

            // Counters for reporting
            val totalPersons = personsArray?.size() ?: 0
            val totalPersonLabels = personLabelsArray?.size() ?: 0
            val totalNoteLabels = noteLabelsArray?.size() ?: 0
            val totalNotes = notesArray?.size() ?: 0
            val totalPersonLabelCrossRefs = personLabelCrossRefsArray?.size() ?: 0
            val totalNoteLabelCrossRefs = noteLabelCrossRefsArray?.size() ?: 0

            var personsImported = 0
            var personLabelsImported = 0
            var noteLabelsImported = 0
            var notesImported = 0
            var personLabelCrossRefsImported = 0
            var noteLabelCrossRefsImported = 0

            database.withTransaction {
                if (mode == ImportMode.Replace) {
                    noteLabelDao.deleteAllNoteLabelCrossRefs()
                    personLabelDao.deleteAllPersonLabelCrossRefs()
                    noteDao.deleteAllNotes()
                    noteLabelDao.deleteAllNoteLabels()
                    personLabelDao.deleteAllPersonLabels()
                    personDao.deleteAllPersons()
                } else {
                    val existingPersonLabels = personLabelDao.getAllLabelsOnce()
                    val existingNoteLabels = noteLabelDao.getAllLabelsOnce()
                    for (pl in existingPersonLabels) {
                        val n = normalizeLabelName(pl.labelName)
                        val c = normalizeColor(pl.colorCode, "#FF6B6B")
                        canonicalPersonMap[LabelKey(n, c)] = pl.id
                    }
                    for (nl in existingNoteLabels) {
                        val n = normalizeLabelName(nl.labelName)
                        val c = normalizeColor(nl.colorCode, "#808080")
                        canonicalNoteMap[LabelKey(n, c)] = nl.id
                    }
                }

                // Insert persons with new autogenerated IDs
                if (personsArray != null) {
                    for (element in personsArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject

                            if (!obj.has("id") || !obj.has("name")) continue
                            val idElement = obj.get("id")
                            if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                            val oldId = idElement.asInt

                            val nameElement = obj.get("name")
                            if (nameElement.isJsonNull) continue
                            val name = nameElement.asString.trim()
                            if (name.isEmpty()) continue

                            val newId = personDao.insertPerson(
                                Person(
                                    name = name
                                )
                            ).toInt()

                            personIdMap[oldId] = newId
                            personsImported++
                        } catch (_: Exception) {
                            // Skip invalid person entries
                            continue
                        }
                    }
                }

                // Insert person labels with new autogenerated IDs
                if (personLabelsArray != null) {
                    for (element in personLabelsArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject

                            if (!obj.has("id") || !obj.has("labelName")) continue
                            val idElement = obj.get("id")
                            if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                            val oldId = idElement.asInt

                            val labelNameElement = obj.get("labelName")
                            if (labelNameElement.isJsonNull) continue
                            val labelName = labelNameElement.asString.trim()
                            if (labelName.isEmpty()) continue

                            val colorCode = if (obj.has("colorCode") && !obj.get("colorCode").isJsonNull) {
                                obj.get("colorCode").asString
                            } else {
                                "#FF6B6B"
                            }

                            if (mode == ImportMode.Merge) {
                                val n = normalizeLabelName(labelName)
                                val c = normalizeColor(colorCode, "#FF6B6B")
                                val existingCanonicalId = canonicalPersonMap[LabelKey(n, c)]
                                if (existingCanonicalId != null) {
                                    personLabelIdMap[oldId] = existingCanonicalId
                                    continue
                                }
                            }

                            val key = LabelKey(labelName, colorCode)
                            val existingNewId = personLabelKeyMap[key]
                            if (existingNewId != null) {
                                personLabelIdMap[oldId] = existingNewId
                                continue
                            }

                            val newId = personLabelDao.insertLabel(
                                PersonLabel(
                                    labelName = labelName,
                                    colorCode = colorCode
                                )
                            ).toInt()

                            personLabelKeyMap[key] = newId
                            personLabelIdMap[oldId] = newId
                            if (mode == ImportMode.Merge) {
                                val n = normalizeLabelName(labelName)
                                val c = normalizeColor(colorCode, "#FF6B6B")
                                canonicalPersonMap[LabelKey(n, c)] = newId
                            }
                            personLabelsImported++
                        } catch (_: Exception) {
                            // Skip invalid person label entries
                            continue
                        }
                    }
                }

                // Insert note labels with new autogenerated IDs
                if (noteLabelsArray != null) {
                    for (element in noteLabelsArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject

                            if (!obj.has("id") || !obj.has("labelName")) continue
                            val idElement = obj.get("id")
                            if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                            val oldId = idElement.asInt

                            val labelNameElement = obj.get("labelName")
                            if (labelNameElement.isJsonNull) continue
                            val labelName = labelNameElement.asString.trim()
                            if (labelName.isEmpty()) continue

                            val colorCode = if (obj.has("colorCode") && !obj.get("colorCode").isJsonNull) {
                                obj.get("colorCode").asString
                            } else {
                                "#808080"
                            }

                            if (mode == ImportMode.Merge) {
                                val n = normalizeLabelName(labelName)
                                val c = normalizeColor(colorCode, "#808080")
                                val existingCanonicalId = canonicalNoteMap[LabelKey(n, c)]
                                if (existingCanonicalId != null) {
                                    noteLabelIdMap[oldId] = existingCanonicalId
                                    continue
                                }
                            }

                            val key = LabelKey(labelName, colorCode)
                            val existingNewId = noteLabelKeyMap[key]
                            if (existingNewId != null) {
                                noteLabelIdMap[oldId] = existingNewId
                                continue
                            }

                            val newId = noteLabelDao.insertLabel(
                                NoteLabel(
                                    labelName = labelName,
                                    colorCode = colorCode
                                )
                            ).toInt()

                            noteLabelKeyMap[key] = newId
                            noteLabelIdMap[oldId] = newId
                            if (mode == ImportMode.Merge) {
                                val n = normalizeLabelName(labelName)
                                val c = normalizeColor(colorCode, "#808080")
                                canonicalNoteMap[LabelKey(n, c)] = newId
                            }
                            noteLabelsImported++
                        } catch (_: Exception) {
                            // Skip invalid note label entries
                            continue
                        }
                    }
                }

                // Insert notes with new autogenerated IDs and remapped foreign keys
                if (notesArray != null) {
                    for (element in notesArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject

                            if (!obj.has("id") || !obj.has("personId")) continue
                            val idElement = obj.get("id")
                            val personIdElement = obj.get("personId")
                            if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                            if (!personIdElement.isJsonPrimitive || !personIdElement.asJsonPrimitive.isNumber) continue

                            val oldId = idElement.asInt
                            val oldPersonId = personIdElement.asInt

                            val newPersonId = personIdMap[oldPersonId] ?: continue

                            val title = if (obj.has("title") && !obj.get("title").isJsonNull) {
                                obj.get("title").asString
                            } else {
                                ""
                            }

                            val text = if (obj.has("text") && !obj.get("text").isJsonNull) {
                                obj.get("text").asString
                            } else {
                                ""
                            }

                            val oldLabelId = if (obj.has("labelId") && !obj.get("labelId").isJsonNull) {
                                val labelElement = obj.get("labelId")
                                if (labelElement.isJsonPrimitive && labelElement.asJsonPrimitive.isNumber) {
                                    labelElement.asInt
                                } else {
                                    null
                                }
                            } else {
                                null
                            }

                            val newLabelId = oldLabelId?.let { noteLabelIdMap[it] }

                            val newId = noteDao.insertNote(
                                Note(
                                    personId = newPersonId,
                                    title = title,
                                    text = text,
                                    labelId = newLabelId
                                )
                            ).toInt()

                            noteIdMap[oldId] = newId
                            notesImported++
                        } catch (_: Exception) {
                            // Skip invalid note entries
                            continue
                        }
                    }
                }

                // Insert person-label cross references with remapped IDs
                if (personLabelCrossRefsArray != null) {
                    for (element in personLabelCrossRefsArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject
                            if (!obj.has("personId") || !obj.has("labelId")) continue

                            val personIdElement = obj.get("personId")
                            val labelIdElement = obj.get("labelId")
                            if (!personIdElement.isJsonPrimitive || !personIdElement.asJsonPrimitive.isNumber) continue
                            if (!labelIdElement.isJsonPrimitive || !labelIdElement.asJsonPrimitive.isNumber) continue

                            val oldPersonId = personIdElement.asInt
                            val oldLabelId = labelIdElement.asInt

                            val newPersonId = personIdMap[oldPersonId] ?: continue
                            val newLabelId = personLabelIdMap[oldLabelId] ?: continue

                            personLabelDao.assignLabelToPerson(
                                PersonLabelCrossRef(
                                    personId = newPersonId,
                                    labelId = newLabelId
                                )
                            )
                            personLabelCrossRefsImported++
                        } catch (_: Exception) {
                            // Skip invalid person-label cross refs
                            continue
                        }
                    }
                }

                // Insert note-label cross references with remapped IDs
                if (noteLabelCrossRefsArray != null) {
                    for (element in noteLabelCrossRefsArray) {
                        try {
                            if (!element.isJsonObject) continue
                            val obj = element.asJsonObject
                            if (!obj.has("noteId") || !obj.has("labelId")) continue

                            val noteIdElement = obj.get("noteId")
                            val labelIdElement = obj.get("labelId")
                            if (!noteIdElement.isJsonPrimitive || !noteIdElement.asJsonPrimitive.isNumber) continue
                            if (!labelIdElement.isJsonPrimitive || !labelIdElement.asJsonPrimitive.isNumber) continue

                            val oldNoteId = noteIdElement.asInt
                            val oldLabelId = labelIdElement.asInt

                            val newNoteId = noteIdMap[oldNoteId] ?: continue
                            val newLabelId = noteLabelIdMap[oldLabelId] ?: continue

                            noteLabelDao.assignLabelToNote(
                                NoteLabelCrossRef(
                                    noteId = newNoteId,
                                    labelId = newLabelId
                                )
                            )
                            noteLabelCrossRefsImported++
                        } catch (_: Exception) {
                            // Skip invalid note-label cross refs
                            continue
                        }
                    }
                }
            }

            // Build summary message
            val skippedPersons = (totalPersons - personsImported).coerceAtLeast(0)
            val skippedPersonLabels = (totalPersonLabels - personLabelsImported).coerceAtLeast(0)
            val skippedNoteLabels = (totalNoteLabels - noteLabelsImported).coerceAtLeast(0)
            val skippedNotes = (totalNotes - notesImported).coerceAtLeast(0)
            val skippedPersonLabelCrossRefs = (totalPersonLabelCrossRefs - personLabelCrossRefsImported).coerceAtLeast(0)
            val skippedNoteLabelCrossRefs = (totalNoteLabelCrossRefs - noteLabelCrossRefsImported).coerceAtLeast(0)

            val anySkipped = skippedPersons > 0 || skippedPersonLabels > 0 || skippedNoteLabels > 0 ||
                    skippedNotes > 0 || skippedPersonLabelCrossRefs > 0 || skippedNoteLabelCrossRefs > 0

            val parts = mutableListOf<String>()
            if (totalPersons > 0) {
                parts.add("Persons: $personsImported imported" + if (skippedPersons > 0) ", $skippedPersons skipped" else "")
            }
            if (totalPersonLabels > 0) {
                parts.add("Person labels: $personLabelsImported imported" + if (skippedPersonLabels > 0) ", $skippedPersonLabels skipped" else "")
            }
            if (totalNotes > 0) {
                parts.add("Notes: $notesImported imported" + if (skippedNotes > 0) ", $skippedNotes skipped" else "")
            }
            if (totalNoteLabels > 0) {
                parts.add("Note labels: $noteLabelsImported imported" + if (skippedNoteLabels > 0) ", $skippedNoteLabels skipped" else "")
            }
            if (totalPersonLabelCrossRefs > 0) {
                parts.add("Person-label links: $personLabelCrossRefsImported imported" + if (skippedPersonLabelCrossRefs > 0) ", $skippedPersonLabelCrossRefs skipped" else "")
            }
            if (totalNoteLabelCrossRefs > 0) {
                parts.add("Note-label links: $noteLabelCrossRefsImported imported" + if (skippedNoteLabelCrossRefs > 0) ", $skippedNoteLabelCrossRefs skipped" else "")
            }

            val message = if (parts.isEmpty()) {
                if (anySkipped) {
                    "✓ Import completed with some skipped items."
                } else {
                    "✓ Import successful (no data to import)."
                }
            } else {
                val base = "✓ Import successful. " + parts.joinToString(" • ")
                if (anySkipped) {
                    "$base (Some items were skipped because they were invalid or referenced missing persons/labels.)"
                } else {
                    base
                }
            }

            ImportResult(
                success = true,
                message = message,
                errorType = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                message = "✗ Import failed: ${e.message ?: "Unknown error"}",
                errorType = ImportErrorType.INTERNAL_ERROR
            )
        }
    }

    fun saveToFile(data: String, fileName: String = "pep_notes_backup.json"): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            file.writeText(data)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveToFileResult(data: String, fileName: String = "pep_notes_backup.json"): FileWriteResult {
        return try {
            val dir = context.filesDir
            val file = File(dir, fileName)
            file.parentFile?.mkdirs()
            file.writeText(data)
            FileWriteResult.Success(file = file)
        } catch (e: SecurityException) {
            FileWriteResult.Error(IoErrorType.PERMISSION_DENIED, e)
        } catch (e: FileNotFoundException) {
            FileWriteResult.Error(IoErrorType.FILE_NOT_FOUND, e)
        } catch (e: IOException) {
            val msg = e.message ?: ""
            if (msg.contains("No space", ignoreCase = true) || msg.contains("ENOSPC", ignoreCase = true)) {
                FileWriteResult.Error(IoErrorType.NO_SPACE, e)
            } else {
                FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
            }
        } catch (e: Exception) {
            FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
        }
    }

    fun readFromFile(fileName: String): String? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save backup file to external storage (Downloads folder)
     * Returns the file URI if successful, null otherwise
     */
    fun saveToExternalStorage(data: String): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "pep_notes_backup_$timeStamp.json"
            
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ use app-specific external files directory
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: return null
                File(dir, fileName)
            } else {
                // For older versions use Downloads folder
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            }
            
            file.parentFile?.mkdirs()
            file.writeText(data)
            
            // Return file URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToInternalStorageStreaming(fileName: String = "pep_notes_backup.json"): FileWriteResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(context.filesDir, fileName)
            file.parentFile?.mkdirs()

            val appVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

            BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).use { bw ->
                val jw = JsonWriter(bw)
                jw.beginObject()
                jw.name("appId").value(context.packageName)
                jw.name("schemaVersion").value(MAX_SCHEMA_VERSION.toLong())
                jw.name("appVersion").value(appVersion)
                jw.name("createdAt").value(createdAt)

                val persons = database.personDao().getAllPersons().first()
                val personLabels = database.personLabelDao().getAllLabels().first()
                val noteLabels = database.noteLabelDao().getAllLabels().first()
                val personLabelCrossRefs = database.personLabelDao().getAllPersonLabelCrossRefsOnce()
                val noteLabelCrossRefs = database.noteLabelDao().getAllNoteLabelCrossRefsOnce()

                jw.name("persons")
                jw.beginArray()
                for (p in persons) {
                    gson.toJson(p, Person::class.java, jw)
                }
                jw.endArray()

                jw.name("personLabels")
                jw.beginArray()
                for (pl in personLabels) {
                    gson.toJson(pl, PersonLabel::class.java, jw)
                }
                jw.endArray()

                jw.name("notes")
                jw.beginArray()
                for (person in persons) {
                    val notesForPerson = database.noteDao().getNotesForPerson(person.id).first()
                    for (n in notesForPerson) {
                        gson.toJson(n, Note::class.java, jw)
                    }
                }
                jw.endArray()

                jw.name("noteLabels")
                jw.beginArray()
                for (nl in noteLabels) {
                    gson.toJson(nl, NoteLabel::class.java, jw)
                }
                jw.endArray()

                jw.name("personLabelCrossRefs")
                jw.beginArray()
                for (cr in personLabelCrossRefs) {
                    gson.toJson(cr, PersonLabelCrossRef::class.java, jw)
                }
                jw.endArray()

                jw.name("noteLabelCrossRefs")
                jw.beginArray()
                for (cr in noteLabelCrossRefs) {
                    gson.toJson(cr, NoteLabelCrossRef::class.java, jw)
                }
                jw.endArray()

                jw.endObject()
                bw.flush()
            }
            FileWriteResult.Success(file = file)
        } catch (e: SecurityException) {
            FileWriteResult.Error(IoErrorType.PERMISSION_DENIED, e)
        } catch (e: FileNotFoundException) {
            FileWriteResult.Error(IoErrorType.FILE_NOT_FOUND, e)
        } catch (e: IOException) {
            val msg = e.message ?: ""
            if (msg.contains("No space", ignoreCase = true) || msg.contains("ENOSPC", ignoreCase = true)) {
                FileWriteResult.Error(IoErrorType.NO_SPACE, e)
            } else {
                FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
            }
        } catch (e: Exception) {
            FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
        }
    }

    suspend fun exportToExternalStorageStreaming(): FileWriteResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "pep_notes_backup_$timeStamp.json"

            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: return@withContext FileWriteResult.Error(IoErrorType.FILESYSTEM_UNAVAILABLE)
                File(dir, fileName)
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            }

            file.parentFile?.mkdirs()

            val appVersion = try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "unknown"
            } catch (e: Exception) {
                "unknown"
            }
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

            BufferedWriter(OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)).use { bw ->
                val jw = JsonWriter(bw)
                jw.beginObject()
                jw.name("appId").value(context.packageName)
                jw.name("schemaVersion").value(MAX_SCHEMA_VERSION.toLong())
                jw.name("appVersion").value(appVersion)
                jw.name("createdAt").value(createdAt)

                val persons = database.personDao().getAllPersons().first()
                val personLabels = database.personLabelDao().getAllLabels().first()
                val noteLabels = database.noteLabelDao().getAllLabels().first()
                val personLabelCrossRefs = database.personLabelDao().getAllPersonLabelCrossRefsOnce()
                val noteLabelCrossRefs = database.noteLabelDao().getAllNoteLabelCrossRefsOnce()

                jw.name("persons")
                jw.beginArray()
                for (p in persons) {
                    gson.toJson(p, Person::class.java, jw)
                }
                jw.endArray()

                jw.name("personLabels")
                jw.beginArray()
                for (pl in personLabels) {
                    gson.toJson(pl, PersonLabel::class.java, jw)
                }
                jw.endArray()

                jw.name("notes")
                jw.beginArray()
                for (person in persons) {
                    val notesForPerson = database.noteDao().getNotesForPerson(person.id).first()
                    for (n in notesForPerson) {
                        gson.toJson(n, Note::class.java, jw)
                    }
                }
                jw.endArray()

                jw.name("noteLabels")
                jw.beginArray()
                for (nl in noteLabels) {
                    gson.toJson(nl, NoteLabel::class.java, jw)
                }
                jw.endArray()

                jw.name("personLabelCrossRefs")
                jw.beginArray()
                for (cr in personLabelCrossRefs) {
                    gson.toJson(cr, PersonLabelCrossRef::class.java, jw)
                }
                jw.endArray()

                jw.name("noteLabelCrossRefs")
                jw.beginArray()
                for (cr in noteLabelCrossRefs) {
                    gson.toJson(cr, NoteLabelCrossRef::class.java, jw)
                }
                jw.endArray()

                jw.endObject()
                bw.flush()
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            FileWriteResult.Success(uri = uri, file = file)
        } catch (e: SecurityException) {
            FileWriteResult.Error(IoErrorType.PERMISSION_DENIED, e)
        } catch (e: FileNotFoundException) {
            FileWriteResult.Error(IoErrorType.FILE_NOT_FOUND, e)
        } catch (e: IOException) {
            val msg = e.message ?: ""
            if (msg.contains("No space", ignoreCase = true) || msg.contains("ENOSPC", ignoreCase = true)) {
                FileWriteResult.Error(IoErrorType.NO_SPACE, e)
            } else {
                FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
            }
        } catch (e: Exception) {
            FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
        }
    }

    fun saveToExternalStorageResult(data: String): FileWriteResult {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "pep_notes_backup_$timeStamp.json"

            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    ?: return FileWriteResult.Error(IoErrorType.FILESYSTEM_UNAVAILABLE)
                File(dir, fileName)
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            }

            file.parentFile?.mkdirs()
            file.writeText(data)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            FileWriteResult.Success(uri = uri, file = file)
        } catch (e: SecurityException) {
            FileWriteResult.Error(IoErrorType.PERMISSION_DENIED, e)
        } catch (e: FileNotFoundException) {
            FileWriteResult.Error(IoErrorType.FILE_NOT_FOUND, e)
        } catch (e: IOException) {
            val msg = e.message ?: ""
            if (msg.contains("No space", ignoreCase = true) || msg.contains("ENOSPC", ignoreCase = true)) {
                FileWriteResult.Error(IoErrorType.NO_SPACE, e)
            } else {
                FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
            }
        } catch (e: Exception) {
            FileWriteResult.Error(IoErrorType.WRITE_ERROR, e)
        }
    }

    /**
     * Get share intent to export data to other apps
     */
    fun getShareIntent(fileUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Read JSON file from URI (used for import from file picker)
     */
    suspend fun readFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun readFromUriResult(uri: Uri): FileReadResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val scheme = uri.scheme
            if (scheme.isNullOrEmpty()) {
                FileReadResult.Error(IoErrorType.INVALID_URI)
            } else {
                val data = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (data == null) {
                    FileReadResult.Error(IoErrorType.FILE_NOT_FOUND)
                } else {
                    FileReadResult.Success(data)
                }
            }
        } catch (e: SecurityException) {
            FileReadResult.Error(IoErrorType.PERMISSION_DENIED, e)
        } catch (e: FileNotFoundException) {
            FileReadResult.Error(IoErrorType.FILE_NOT_FOUND, e)
        } catch (e: IOException) {
            FileReadResult.Error(IoErrorType.READ_ERROR, e)
        } catch (e: Exception) {
            FileReadResult.Error(IoErrorType.UNKNOWN, e)
        }
    }

    suspend fun importDataFromUriStreaming(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult(
                    success = false,
                    message = "✗ Import failed: Failed to read file",
                    errorType = ImportErrorType.PARSE_ERROR
                )

            val reader = com.google.gson.stream.JsonReader(InputStreamReader(input, StandardCharsets.UTF_8))
            
            val personDao = database.personDao()
            val personLabelDao = database.personLabelDao()
            val noteDao = database.noteDao()
            val noteLabelDao = database.noteLabelDao()
            
            val personIdMap = mutableMapOf<Int, Int>()
            val personLabelIdMap = mutableMapOf<Int, Int>()
            val noteIdMap = mutableMapOf<Int, Int>()
            val noteLabelIdMap = mutableMapOf<Int, Int>()

            data class LabelKey(val name: String, val color: String)
            val personLabelKeyMap = mutableMapOf<LabelKey, Int>()
            val noteLabelKeyMap = mutableMapOf<LabelKey, Int>()

            
            var totalPersons = 0
            var totalPersonLabels = 0
            var totalNoteLabels = 0
            var totalNotes = 0
            var totalPersonLabelCrossRefs = 0
            var totalNoteLabelCrossRefs = 0

            var personsImported = 0
            var personLabelsImported = 0
            var noteLabelsImported = 0
            var notesImported = 0
            var personLabelCrossRefsImported = 0
            var noteLabelCrossRefsImported = 0

            database.withTransaction {
                // Clear existing data in a safe order
                noteLabelDao.deleteAllNoteLabelCrossRefs()
                personLabelDao.deleteAllPersonLabelCrossRefs()
                noteDao.deleteAllNotes()
                noteLabelDao.deleteAllNoteLabels()
                personLabelDao.deleteAllPersonLabels()
                personDao.deleteAllPersons()

                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "persons" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalPersons++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("id") || !obj.has("name")) continue
                                    val idElement = obj.get("id")
                                    if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                                    val oldId = idElement.asInt

                                    val nameElement = obj.get("name")
                                    if (nameElement.isJsonNull) continue
                                    val name = nameElement.asString.trim()
                                    if (name.isEmpty()) continue

                                    val newId = personDao.insertPerson(
                                        Person(name = name)
                                    ).toInt()
                                    personIdMap[oldId] = newId
                                    personsImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        "personLabels" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalPersonLabels++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("id") || !obj.has("labelName")) continue
                                    val idElement = obj.get("id")
                                    if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                                    val oldId = idElement.asInt

                                    val labelNameElement = obj.get("labelName")
                                    if (labelNameElement.isJsonNull) continue
                                    val labelName = labelNameElement.asString.trim()
                                    if (labelName.isEmpty()) continue

                                    val colorCode = if (obj.has("colorCode") && !obj.get("colorCode").isJsonNull) {
                                        obj.get("colorCode").asString
                                    } else {
                                        "#FF6B6B"
                                    }

                                    val key = LabelKey(labelName, colorCode)
                                    val existingNewId = personLabelKeyMap[key]
                                    if (existingNewId != null) {
                                        personLabelIdMap[oldId] = existingNewId
                                        continue
                                    }

                                    val newId = personLabelDao.insertLabel(
                                        PersonLabel(labelName = labelName, colorCode = colorCode)
                                    ).toInt()
                                    personLabelKeyMap[key] = newId
                                    personLabelIdMap[oldId] = newId
                                    personLabelsImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        "notes" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalNotes++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("id") || !obj.has("personId")) continue
                                    val idElement = obj.get("id")
                                    val personIdElement = obj.get("personId")
                                    if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                                    if (!personIdElement.isJsonPrimitive || !personIdElement.asJsonPrimitive.isNumber) continue

                                    val oldId = idElement.asInt
                                    val oldPersonId = personIdElement.asInt
                                    val newPersonId = personIdMap[oldPersonId] ?: continue

                                    val title = if (obj.has("title") && !obj.get("title").isJsonNull) obj.get("title").asString else ""
                                    val text = if (obj.has("text") && !obj.get("text").isJsonNull) obj.get("text").asString else ""

                                    val oldLabelId = if (obj.has("labelId") && !obj.get("labelId").isJsonNull) {
                                        val labelElement = obj.get("labelId")
                                        if (labelElement.isJsonPrimitive && labelElement.asJsonPrimitive.isNumber) labelElement.asInt else null
                                    } else null

                                    val newLabelId = oldLabelId?.let { noteLabelIdMap[it] }

                                    val newId = noteDao.insertNote(
                                        Note(
                                            personId = newPersonId,
                                            title = title,
                                            text = text,
                                            labelId = newLabelId
                                        )
                                    ).toInt()
                                    noteIdMap[oldId] = newId
                                    notesImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        "noteLabels" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalNoteLabels++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("id") || !obj.has("labelName")) continue
                                    val idElement = obj.get("id")
                                    if (!idElement.isJsonPrimitive || !idElement.asJsonPrimitive.isNumber) continue
                                    val oldId = idElement.asInt

                                    val labelNameElement = obj.get("labelName")
                                    if (labelNameElement.isJsonNull) continue
                                    val labelName = labelNameElement.asString.trim()
                                    if (labelName.isEmpty()) continue

                                    val colorCode = if (obj.has("colorCode") && !obj.get("colorCode").isJsonNull) {
                                        obj.get("colorCode").asString
                                    } else {
                                        "#808080"
                                    }

                                    val key = LabelKey(labelName, colorCode)
                                    val existingNewId = noteLabelKeyMap[key]
                                    if (existingNewId != null) {
                                        noteLabelIdMap[oldId] = existingNewId
                                        continue
                                    }

                                    val newId = noteLabelDao.insertLabel(
                                        NoteLabel(labelName = labelName, colorCode = colorCode)
                                    ).toInt()
                                    noteLabelKeyMap[key] = newId
                                    noteLabelIdMap[oldId] = newId
                                    noteLabelsImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        "personLabelCrossRefs" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalPersonLabelCrossRefs++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("personId") || !obj.has("labelId")) continue
                                    val personIdElement = obj.get("personId")
                                    val labelIdElement = obj.get("labelId")
                                    if (!personIdElement.isJsonPrimitive || !personIdElement.asJsonPrimitive.isNumber) continue
                                    if (!labelIdElement.isJsonPrimitive || !labelIdElement.asJsonPrimitive.isNumber) continue

                                    val oldPersonId = personIdElement.asInt
                                    val oldLabelId = labelIdElement.asInt
                                    val newPersonId = personIdMap[oldPersonId] ?: continue
                                    val newLabelId = personLabelIdMap[oldLabelId] ?: continue

                                    personLabelDao.assignLabelToPerson(
                                        PersonLabelCrossRef(personId = newPersonId, labelId = newLabelId)
                                    )
                                    personLabelCrossRefsImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        "noteLabelCrossRefs" -> {
                            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); continue }
                            reader.beginArray()
                            while (reader.hasNext()) {
                                totalNoteLabelCrossRefs++
                                try {
                                    val obj: JsonObject = gson.fromJson(reader, JsonObject::class.java)
                                    if (!obj.has("noteId") || !obj.has("labelId")) continue
                                    val noteIdElement = obj.get("noteId")
                                    val labelIdElement = obj.get("labelId")
                                    if (!noteIdElement.isJsonPrimitive || !noteIdElement.asJsonPrimitive.isNumber) continue
                                    if (!labelIdElement.isJsonPrimitive || !labelIdElement.asJsonPrimitive.isNumber) continue

                                    val oldNoteId = noteIdElement.asInt
                                    val oldLabelId = labelIdElement.asInt
                                    val newNoteId = noteIdMap[oldNoteId] ?: continue
                                    val newLabelId = noteLabelIdMap[oldLabelId] ?: continue

                                    noteLabelDao.assignLabelToNote(
                                        NoteLabelCrossRef(noteId = newNoteId, labelId = newLabelId)
                                    )
                                    noteLabelCrossRefsImported++
                                } catch (_: Exception) {
                                    continue
                                }
                            }
                            reader.endArray()
                        }
                        else -> {
                            reader.skipValue()
                        }
                    }
                }
                reader.endObject()
                reader.close()
            }

            val skippedPersons = (totalPersons - personsImported).coerceAtLeast(0)
            val skippedPersonLabels = (totalPersonLabels - personLabelsImported).coerceAtLeast(0)
            val skippedNoteLabels = (totalNoteLabels - noteLabelsImported).coerceAtLeast(0)
            val skippedNotes = (totalNotes - notesImported).coerceAtLeast(0)
            val skippedPersonLabelCrossRefs = (totalPersonLabelCrossRefs - personLabelCrossRefsImported).coerceAtLeast(0)
            val skippedNoteLabelCrossRefs = (totalNoteLabelCrossRefs - noteLabelCrossRefsImported).coerceAtLeast(0)

            val anySkipped = skippedPersons > 0 || skippedPersonLabels > 0 || skippedNoteLabels > 0 ||
                    skippedNotes > 0 || skippedPersonLabelCrossRefs > 0 || skippedNoteLabelCrossRefs > 0

            val parts = mutableListOf<String>()
            if (totalPersons > 0) parts.add("Persons: $personsImported imported" + if (skippedPersons > 0) ", $skippedPersons skipped" else "")
            if (totalPersonLabels > 0) parts.add("Person labels: $personLabelsImported imported" + if (skippedPersonLabels > 0) ", $skippedPersonLabels skipped" else "")
            if (totalNotes > 0) parts.add("Notes: $notesImported imported" + if (skippedNotes > 0) ", $skippedNotes skipped" else "")
            if (totalNoteLabels > 0) parts.add("Note labels: $noteLabelsImported imported" + if (skippedNoteLabels > 0) ", $skippedNoteLabels skipped" else "")
            if (totalPersonLabelCrossRefs > 0) parts.add("Person-label links: $personLabelCrossRefsImported imported" + if (skippedPersonLabelCrossRefs > 0) ", $skippedPersonLabelCrossRefs skipped" else "")
            if (totalNoteLabelCrossRefs > 0) parts.add("Note-label links: $noteLabelCrossRefsImported imported" + if (skippedNoteLabelCrossRefs > 0) ", $skippedNoteLabelCrossRefs skipped" else "")

            val message = if (parts.isEmpty()) {
                if (anySkipped) {
                    "✓ Import completed with some skipped items."
                } else {
                    "✓ Import successful (no data to import)."
                }
            } else {
                val base = "✓ Import successful. " + parts.joinToString(" • ")
                if (anySkipped) "$base (Some items were skipped because they were invalid or referenced missing persons/labels.)" else base
            }

            ImportResult(success = true, message = message, errorType = null)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(
                success = false,
                message = "✗ Import failed: ${e.message ?: "Unknown error"}",
                errorType = ImportErrorType.INTERNAL_ERROR
            )
        }
    }

    /**
     * Validate if the JSON file is a valid backup file
     */
    fun isValidBackupFile(jsonString: String): Boolean {
        return try {
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

            val hasAppId = jsonObject.has("appId")
            val hasSchemaVersion = jsonObject.has("schemaVersion")

            if (hasAppId || hasSchemaVersion) {
                val appIdEl = jsonObject.get("appId")
                val schemaEl = jsonObject.get("schemaVersion")
                if (!hasAppId || appIdEl == null || !appIdEl.isJsonPrimitive) return false
                if (!hasSchemaVersion || schemaEl == null || !schemaEl.isJsonPrimitive || !schemaEl.asJsonPrimitive.isNumber) return false

                val appId = appIdEl.asString
                val schemaVersion = schemaEl.asInt
                val appIdMatches = appId == context.packageName || appId == APP_ID
                val schemaInRange = schemaVersion in MIN_SCHEMA_VERSION..MAX_SCHEMA_VERSION
                if (!appIdMatches || !schemaInRange) return false
            } else {
                val arrayKeys = listOf("persons", "personLabels", "notes", "noteLabels")
                val anyArrayPresent = arrayKeys.any { key -> jsonObject.has(key) && jsonObject.get(key).isJsonArray }
                if (!anyArrayPresent) return false
            }

            val keysShouldBeArrays = listOf(
                "persons",
                "personLabels",
                "notes",
                "noteLabels",
                "personLabelCrossRefs",
                "noteLabelCrossRefs"
            )
            for (k in keysShouldBeArrays) {
                if (jsonObject.has(k) && !jsonObject.get(k).isJsonArray) return false
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
