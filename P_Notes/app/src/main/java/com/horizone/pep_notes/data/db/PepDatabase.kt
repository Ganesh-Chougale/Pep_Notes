package com.horizone.pep_notes.data.db

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import com.horizone.pep_notes.data.seed.DefaultPersonLabels
import com.horizone.pep_notes.data.seed.DefaultNoteLabels
import com.horizone.pep_notes.util.Converters
import com.horizone.pep_notes.util.normalizeColor
import com.horizone.pep_notes.util.normalizeLabelName


@Database(
    entities = [
        Person::class,
        PersonLabel::class,
        PersonLabelCrossRef::class,
        Note::class,
        NoteLabel::class,
        NoteLabelCrossRef::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PepDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun personLabelDao(): PersonLabelDao
    abstract fun noteDao(): NoteDao
    abstract fun noteLabelDao(): NoteLabelDao

    companion object {
        @Volatile
        private var INSTANCE: PepDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN title TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE person_labels ADD COLUMN colorCode TEXT NOT NULL DEFAULT '#FF6B6B'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE note_labels ADD COLUMN colorCode TEXT NOT NULL DEFAULT '#808080'")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN labelId INTEGER")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Normalize and deduplicate person labels using canonical (name, color)
                run {
                    val idForKey = mutableMapOf<Pair<String, String>, Int>()
                    val cursor = database.query("SELECT id, labelName, colorCode FROM person_labels")
                    cursor.use { c ->
                        val idIndex = c.getColumnIndex("id")
                        val nameIndex = c.getColumnIndex("labelName")
                        val colorIndex = c.getColumnIndex("colorCode")
                        while (c.moveToNext()) {
                            if (idIndex < 0 || nameIndex < 0 || colorIndex < 0) break
                            val id = c.getInt(idIndex)
                            val rawName = c.getString(nameIndex) ?: continue
                            if (rawName.isBlank()) continue
                            val rawColor = c.getString(colorIndex)

                            val canonicalName = normalizeLabelName(rawName)
                            val canonicalColor = normalizeColor(rawColor, "#FF6B6B")
                            val key = canonicalName to canonicalColor
                            val existingId = idForKey[key]

                            if (existingId == null) {
                                idForKey[key] = id
                                database.execSQL(
                                    "UPDATE person_labels SET labelName = ?, colorCode = ? WHERE id = ?",
                                    arrayOf<Any>(canonicalName, canonicalColor, id)
                                )
                            } else {
                                // Remove cross-refs that would become duplicates after remap
                                database.execSQL(
                                    """
                                    DELETE FROM person_label_cross_ref
                                    WHERE labelId = ? AND personId IN (
                                        SELECT personId FROM person_label_cross_ref WHERE labelId = ?
                                    )
                                    """.trimIndent(),
                                    arrayOf<Any>(id, existingId)
                                )

                                // Remap remaining cross-refs to the canonical label id
                                database.execSQL(
                                    "UPDATE person_label_cross_ref SET labelId = ? WHERE labelId = ?",
                                    arrayOf<Any>(existingId, id)
                                )

                                // Drop the duplicate label row
                                database.execSQL(
                                    "DELETE FROM person_labels WHERE id = ?",
                                    arrayOf<Any>(id)
                                )
                            }
                        }
                    }

                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_person_labels_labelName_colorCode ON person_labels(labelName, colorCode)"
                    )
                }

                // Normalize and deduplicate note labels using canonical (name, color)
                run {
                    val idForKey = mutableMapOf<Pair<String, String>, Int>()
                    val cursor = database.query("SELECT id, labelName, colorCode FROM note_labels")
                    cursor.use { c ->
                        val idIndex = c.getColumnIndex("id")
                        val nameIndex = c.getColumnIndex("labelName")
                        val colorIndex = c.getColumnIndex("colorCode")
                        while (c.moveToNext()) {
                            if (idIndex < 0 || nameIndex < 0 || colorIndex < 0) break
                            val id = c.getInt(idIndex)
                            val rawName = c.getString(nameIndex) ?: continue
                            if (rawName.isBlank()) continue
                            val rawColor = c.getString(colorIndex)

                            val canonicalName = normalizeLabelName(rawName)
                            val canonicalColor = normalizeColor(rawColor, "#808080")
                            val key = canonicalName to canonicalColor
                            val existingId = idForKey[key]

                            if (existingId == null) {
                                idForKey[key] = id
                                database.execSQL(
                                    "UPDATE note_labels SET labelName = ?, colorCode = ? WHERE id = ?",
                                    arrayOf<Any>(canonicalName, canonicalColor, id)
                                )
                            } else {
                                // Remove note-label cross-refs that would become duplicates
                                database.execSQL(
                                    """
                                    DELETE FROM note_label_cross_ref
                                    WHERE labelId = ? AND noteId IN (
                                        SELECT noteId FROM note_label_cross_ref WHERE labelId = ?
                                    )
                                    """.trimIndent(),
                                    arrayOf<Any>(id, existingId)
                                )

                                // Remap remaining note-label cross-refs
                                database.execSQL(
                                    "UPDATE note_label_cross_ref SET labelId = ? WHERE labelId = ?",
                                    arrayOf<Any>(existingId, id)
                                )

                                // Remap direct note.labelId foreign keys
                                database.execSQL(
                                    "UPDATE notes SET labelId = ? WHERE labelId = ?",
                                    arrayOf<Any>(existingId, id)
                                )

                                // Drop the duplicate label row
                                database.execSQL(
                                    "DELETE FROM note_labels WHERE id = ?",
                                    arrayOf<Any>(id)
                                )
                            }
                        }
                    }

                    database.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS index_note_labels_labelName_colorCode ON note_labels(labelName, colorCode)"
                    )
                }
            }
        }

        fun getDatabase(context: Context): PepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PepDatabase::class.java,
                    "pep_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default person labels on database creation
                            DefaultPersonLabels.defaultLabels.forEach { label ->
                                db.execSQL(
                                    "INSERT INTO person_labels (id, labelName, colorCode) VALUES (${label.id}, '${label.labelName}', '${label.colorCode}')"
                                )
                            }
                            // Seed default note labels on database creation
                            DefaultNoteLabels.defaultLabels.forEach { label ->
                                db.execSQL(
                                    "INSERT INTO note_labels (id, labelName, colorCode) VALUES (${label.id}, '${label.labelName}', '${label.colorCode}')"
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
