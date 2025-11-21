package com.horizone.pep_notes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import com.horizone.pep_notes.data.seed.DefaultPersonLabels
import com.horizone.pep_notes.util.Converters

@Database(
    entities = [
        Person::class,
        PersonLabel::class,
        PersonLabelCrossRef::class,
        Note::class,
        NoteLabel::class,
        NoteLabelCrossRef::class
    ],
    version = 3,
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

        fun getDatabase(context: Context): PepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PepDatabase::class.java,
                    "pep_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default labels on database creation
                            DefaultPersonLabels.defaultLabels.forEach { label ->
                                db.execSQL(
                                    "INSERT INTO person_labels (id, labelName, colorCode) VALUES (${label.id}, '${label.labelName}', '${label.colorCode}')"
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
