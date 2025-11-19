package com.horizone.pep_notes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_labels")
data class PersonLabel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val labelName: String
)
