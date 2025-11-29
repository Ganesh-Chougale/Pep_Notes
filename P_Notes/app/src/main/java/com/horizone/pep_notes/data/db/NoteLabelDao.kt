package com.horizone.pep_notes.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.horizone.pep_notes.data.model.NoteLabel
import com.horizone.pep_notes.data.model.NoteLabelCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteLabelDao {
    @Insert
    suspend fun insertLabel(label: NoteLabel): Long

    @Update
    suspend fun updateLabel(label: NoteLabel)

    @Delete
    suspend fun deleteLabel(label: NoteLabel)

    @Query("SELECT * FROM note_labels ORDER BY labelName ASC")
    fun getAllLabels(): Flow<List<NoteLabel>>

    @Query("SELECT * FROM note_labels WHERE id = :id")
    suspend fun getLabelById(id: Int): NoteLabel?

    @Insert
    suspend fun assignLabelToNote(crossRef: NoteLabelCrossRef)

    @Delete
    suspend fun removeLabelFromNote(crossRef: NoteLabelCrossRef)

    @Query("SELECT nl.* FROM note_labels nl INNER JOIN note_label_cross_ref nlcr ON nl.id = nlcr.labelId WHERE nlcr.noteId = :noteId")
    fun getLabelsForNote(noteId: Int): Flow<List<NoteLabel>>

    @Query("DELETE FROM note_labels")
    suspend fun deleteAllNoteLabels()

    @Query("DELETE FROM note_label_cross_ref")
    suspend fun deleteAllNoteLabelCrossRefs()

    @Query("SELECT * FROM note_labels WHERE labelName = :name AND colorCode = :color LIMIT 1")
    suspend fun getByNameColor(name: String, color: String): NoteLabel?

    @Query("SELECT * FROM note_labels")
    suspend fun getAllLabelsOnce(): List<NoteLabel>
}
