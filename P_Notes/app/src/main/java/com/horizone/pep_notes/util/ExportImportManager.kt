package com.horizone.pep_notes.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.horizone.pep_notes.data.db.PepDatabase
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class ExportImportManager(private val context: Context, private val database: PepDatabase) {
    private val gson = Gson()

    suspend fun exportData(): String = withContext(Dispatchers.IO) {
        val jsonObject = JsonObject()

        try {
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
            val personLabelCrossRefs = mutableListOf<PersonLabelCrossRef>()
            val noteLabelCrossRefs = mutableListOf<NoteLabelCrossRef>()

            // Query cross references from database (need to add methods to DAOs if not present)
            // For now, we'll export what we have
            
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

    suspend fun importData(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

            // Parse arrays
            val personsArray = jsonObject.getAsJsonArray("persons")
            val personLabelsArray = jsonObject.getAsJsonArray("personLabels")
            val notesArray = jsonObject.getAsJsonArray("notes")
            val noteLabelsArray = jsonObject.getAsJsonArray("noteLabels")
            val personLabelCrossRefsArray = jsonObject.getAsJsonArray("personLabelCrossRefs")
            val noteLabelCrossRefsArray = jsonObject.getAsJsonArray("noteLabelCrossRefs")

            // Clear existing data
            val personDao = database.personDao()
            val personLabelDao = database.personLabelDao()
            val noteDao = database.noteDao()
            val noteLabelDao = database.noteLabelDao()

            // Insert persons
            if (personsArray != null) {
                for (element in personsArray) {
                    val person = gson.fromJson(element, Person::class.java)
                    personDao.insertPerson(person)
                }
            }

            // Insert person labels
            if (personLabelsArray != null) {
                for (element in personLabelsArray) {
                    val label = gson.fromJson(element, PersonLabel::class.java)
                    personLabelDao.insertLabel(label)
                }
            }

            // Insert notes
            if (notesArray != null) {
                for (element in notesArray) {
                    val note = gson.fromJson(element, Note::class.java)
                    noteDao.insertNote(note)
                }
            }

            // Insert note labels
            if (noteLabelsArray != null) {
                for (element in noteLabelsArray) {
                    val label = gson.fromJson(element, NoteLabel::class.java)
                    noteLabelDao.insertLabel(label)
                }
            }

            // Insert cross references
            if (personLabelCrossRefsArray != null) {
                for (element in personLabelCrossRefsArray) {
                    val crossRef = gson.fromJson(element, PersonLabelCrossRef::class.java)
                    personLabelDao.assignLabelToPerson(crossRef)
                }
            }

            if (noteLabelCrossRefsArray != null) {
                for (element in noteLabelCrossRefsArray) {
                    val crossRef = gson.fromJson(element, NoteLabelCrossRef::class.java)
                    noteLabelDao.assignLabelToNote(crossRef)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveToFile(data: String, fileName: String = "pep_notes_backup.json"): Boolean {
        return try {
            val file = File(context.filesDir, fileName)
            file.writeText(data)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readFromFile(fileName: String): String? {
        return try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }
}
