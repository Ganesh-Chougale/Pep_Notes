package com.horizone.pep_notes.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.horizone.pep_notes.data.model.PersonLabel
import com.horizone.pep_notes.data.model.PersonLabelCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonLabelDao {
    @Insert
    suspend fun insertLabel(label: PersonLabel): Long

    @Update
    suspend fun updateLabel(label: PersonLabel)

    @Delete
    suspend fun deleteLabel(label: PersonLabel)

    @Query("SELECT * FROM person_labels ORDER BY labelName ASC")
    fun getAllLabels(): Flow<List<PersonLabel>>

    @Query("SELECT * FROM person_labels WHERE id = :id")
    suspend fun getLabelById(id: Int): PersonLabel?

    @Insert
    suspend fun assignLabelToPerson(crossRef: PersonLabelCrossRef)

    @Delete
    suspend fun removeLabelFromPerson(crossRef: PersonLabelCrossRef)

    @Query("SELECT pl.* FROM person_labels pl INNER JOIN person_label_cross_ref plcr ON pl.id = plcr.labelId WHERE plcr.personId = :personId")
    fun getLabelsForPerson(personId: Int): Flow<List<PersonLabel>>

    @Query("DELETE FROM person_labels")
    suspend fun deleteAllPersonLabels()

    @Query("DELETE FROM person_label_cross_ref")
    suspend fun deleteAllPersonLabelCrossRefs()

    @Query("SELECT * FROM person_label_cross_ref")
    suspend fun getAllPersonLabelCrossRefsOnce(): List<PersonLabelCrossRef>

    @Query("SELECT * FROM person_labels WHERE labelName = :name AND colorCode = :color LIMIT 1")
    suspend fun getByNameColor(name: String, color: String): PersonLabel?

    @Query("SELECT * FROM person_labels")
    suspend fun getAllLabelsOnce(): List<PersonLabel>
}
