package com.horizone.pep_notes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
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
    version = 1,
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

        fun getDatabase(context: Context): PepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PepDatabase::class.java,
                    "pep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
