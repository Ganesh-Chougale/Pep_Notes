package com.horizone.pep_notes

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.horizone.pep_notes.data.db.PepDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration5To6Test {

    @Test
    fun migrate5To6_deduplicatesLabelsAndRemapsLinks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "pep_migration_5_6_test.db"

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS person_labels (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            labelName TEXT NOT NULL,
                            colorCode TEXT NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS note_labels (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            labelName TEXT NOT NULL,
                            colorCode TEXT NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS person_label_cross_ref (
                            personId INTEGER NOT NULL,
                            labelId INTEGER NOT NULL,
                            PRIMARY KEY(personId, labelId)
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS note_label_cross_ref (
                            noteId INTEGER NOT NULL,
                            labelId INTEGER NOT NULL,
                            PRIMARY KEY(noteId, labelId)
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS notes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            personId INTEGER NOT NULL,
                            title TEXT NOT NULL,
                            text TEXT NOT NULL,
                            labelId INTEGER
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                }
            })
            .build()

        val helperFactory = FrameworkSQLiteOpenHelperFactory()
        val helper = helperFactory.create(config)
        val db = helper.writableDatabase

        db.execSQL("INSERT INTO person_labels (id, labelName, colorCode) VALUES (1, ' Family ', 'ff6b6b')")
        db.execSQL("INSERT INTO person_labels (id, labelName, colorCode) VALUES (2, 'family', '#FF6B6B')")
        db.execSQL("INSERT INTO person_label_cross_ref (personId, labelId) VALUES (1, 1)")
        db.execSQL("INSERT INTO person_label_cross_ref (personId, labelId) VALUES (1, 2)")

        db.execSQL("INSERT INTO note_labels (id, labelName, colorCode) VALUES (10, 'Ideas', '808080')")
        db.execSQL("INSERT INTO note_labels (id, labelName, colorCode) VALUES (11, ' ideas ', '#808080')")
        db.execSQL("INSERT INTO note_label_cross_ref (noteId, labelId) VALUES (100, 10)")
        db.execSQL("INSERT INTO note_label_cross_ref (noteId, labelId) VALUES (100, 11)")
        db.execSQL("INSERT INTO notes (id, personId, title, text, labelId) VALUES (100, 1, 't', 'body', 10)")

        PepDatabase.MIGRATION_5_6.migrate(db)

        var canonicalPersonLabelId = -1
        db.query("SELECT id, labelName, colorCode FROM person_labels").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            canonicalPersonLabelId = cursor.getInt(0)
            assertEquals("family", cursor.getString(1))
            assertEquals("#FF6B6B", cursor.getString(2))
        }

        db.query("SELECT COUNT(*) FROM person_label_cross_ref").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }

        db.query("SELECT personId, labelId FROM person_label_cross_ref").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
            assertEquals(canonicalPersonLabelId, cursor.getInt(1))
        }

        var canonicalNoteLabelId = -1
        db.query("SELECT id, labelName, colorCode FROM note_labels").use { cursor ->
            assertEquals(1, cursor.count)
            cursor.moveToFirst()
            canonicalNoteLabelId = cursor.getInt(0)
            assertEquals("ideas", cursor.getString(1))
            assertEquals("#808080", cursor.getString(2))
        }

        db.query("SELECT COUNT(*) FROM note_label_cross_ref").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }

        db.query("SELECT noteId, labelId FROM note_label_cross_ref").use { cursor ->
            cursor.moveToFirst()
            assertEquals(100, cursor.getInt(0))
            assertEquals(canonicalNoteLabelId, cursor.getInt(1))
        }

        db.query("SELECT labelId FROM notes WHERE id = 100").use { cursor ->
            cursor.moveToFirst()
            assertEquals(canonicalNoteLabelId, cursor.getInt(0))
        }

        db.close()
        helper.close()
    }
}
