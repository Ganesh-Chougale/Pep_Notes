package com.horizone.pep_notes.data.repository

import com.horizone.pep_notes.data.db.NoteDao
import com.horizone.pep_notes.data.db.NoteLabelDao
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteLabelDao: NoteLabelDao
) {
    fun getNotesForPerson(personId: Int): Flow<List<Note>> = noteDao.getNotesForPerson(personId)

    fun searchNotesForPerson(personId: Int, query: String): Flow<List<Note>> =
        noteDao.searchNotesForPerson(personId, query)

    suspend fun getNoteById(id: Int): Note? = noteDao.getNoteById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    fun getLabelsForNote(noteId: Int): Flow<List<NoteLabel>> =
        noteLabelDao.getLabelsForNote(noteId)

    suspend fun assignLabelToNote(noteId: Int, labelId: Int) {
        noteLabelDao.assignLabelToNote(NoteLabelCrossRef(noteId, labelId))
    }

    suspend fun removeLabelFromNote(noteId: Int, labelId: Int) {
        noteLabelDao.removeLabelFromNote(NoteLabelCrossRef(noteId, labelId))
    }
}
