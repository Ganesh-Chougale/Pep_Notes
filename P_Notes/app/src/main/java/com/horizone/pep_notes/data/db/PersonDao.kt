package com.horizone.pep_notes.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.horizone.pep_notes.data.model.Person
import com.horizone.pep_notes.data.model.PersonWithLabels
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: Int): Person?

    @Query("SELECT * FROM persons ORDER BY createdAt DESC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPersons(query: String): Flow<List<Person>>

    @Transaction
    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonWithLabels(id: Int): Flow<PersonWithLabels?>
}
