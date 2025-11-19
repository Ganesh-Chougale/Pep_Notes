package com.horizone.pep_notes.di

import android.content.Context
import com.horizone.pep_notes.data.db.PepDatabase
import com.horizone.pep_notes.data.db.PersonDao
import com.horizone.pep_notes.data.db.PersonLabelDao
import com.horizone.pep_notes.data.db.NoteDao
import com.horizone.pep_notes.data.db.NoteLabelDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providePepDatabase(@ApplicationContext context: Context): PepDatabase {
        return PepDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun providePersonDao(database: PepDatabase): PersonDao = database.personDao()

    @Singleton
    @Provides
    fun providePersonLabelDao(database: PepDatabase): PersonLabelDao = database.personLabelDao()

    @Singleton
    @Provides
    fun provideNoteDao(database: PepDatabase): NoteDao = database.noteDao()

    @Singleton
    @Provides
    fun provideNoteLabelDao(database: PepDatabase): NoteLabelDao = database.noteLabelDao()
}
