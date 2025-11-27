package com.horizone.pep_notes.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.horizone.pep_notes.data.model.Note
import com.horizone.pep_notes.data.model.NoteWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM notes WHERE personId = :personId ORDER BY createdAt DESC")
    fun getNotesForPerson(personId: Int): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE personId = :personId AND text LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchNotesForPerson(personId: Int, query: String): Flow<List<Note>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteWithLabels(id: Int): Flow<NoteWithLabels?>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
