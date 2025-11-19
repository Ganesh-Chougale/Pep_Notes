package com.horizone.pep_notes.data.repository

import com.horizone.pep_notes.data.db.NoteLabelDao
import com.horizone.pep_notes.data.model.NoteLabel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteLabelRepository @Inject constructor(
    private val noteLabelDao: NoteLabelDao
) {
    fun getAllLabels(): Flow<List<NoteLabel>> = noteLabelDao.getAllLabels()

    suspend fun getLabelById(id: Int): NoteLabel? = noteLabelDao.getLabelById(id)

    suspend fun insertLabel(label: NoteLabel): Long = noteLabelDao.insertLabel(label)

    suspend fun updateLabel(label: NoteLabel) = noteLabelDao.updateLabel(label)

    suspend fun deleteLabel(label: NoteLabel) = noteLabelDao.deleteLabel(label)
}
