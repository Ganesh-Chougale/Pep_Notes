package com.horizone.pep_notes.data.repository

import com.horizone.pep_notes.data.db.PersonLabelDao
import com.horizone.pep_notes.data.model.PersonLabel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PersonLabelRepository @Inject constructor(
    private val personLabelDao: PersonLabelDao
) {
    fun getAllLabels(): Flow<List<PersonLabel>> = personLabelDao.getAllLabels()

    suspend fun getLabelById(id: Int): PersonLabel? = personLabelDao.getLabelById(id)

    suspend fun insertLabel(label: PersonLabel): Long = personLabelDao.insertLabel(label)

    suspend fun updateLabel(label: PersonLabel) = personLabelDao.updateLabel(label)

    suspend fun deleteLabel(label: PersonLabel) = personLabelDao.deleteLabel(label)
}
