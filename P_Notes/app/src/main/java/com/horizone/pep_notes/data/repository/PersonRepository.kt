package com.horizone.pep_notes.data.repository

import com.horizone.pep_notes.data.db.PersonDao
import com.horizone.pep_notes.data.db.PersonLabelDao
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PersonRepository @Inject constructor(
    private val personDao: PersonDao,
    private val personLabelDao: PersonLabelDao
) {
    fun getAllPersons(): Flow<List<Person>> = personDao.getAllPersons()

    fun searchPersons(query: String): Flow<List<Person>> = personDao.searchPersons(query)

    suspend fun getPersonById(id: Int): Person? = personDao.getPersonById(id)

    suspend fun insertPerson(person: Person): Long = personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)

    suspend fun deletePerson(person: Person) = personDao.deletePerson(person)

    fun getLabelsForPerson(personId: Int): Flow<List<PersonLabel>> =
        personLabelDao.getLabelsForPerson(personId)

    suspend fun assignLabelToPerson(personId: Int, labelId: Int) {
        personLabelDao.assignLabelToPerson(PersonLabelCrossRef(personId, labelId))
    }

    suspend fun removeLabelFromPerson(personId: Int, labelId: Int) {
        personLabelDao.removeLabelFromPerson(PersonLabelCrossRef(personId, labelId))
    }
}
