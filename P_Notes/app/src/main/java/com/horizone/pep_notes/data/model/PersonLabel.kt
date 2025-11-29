package com.horizone.pep_notes.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "person_labels",
    indices = [Index(value = ["labelName", "colorCode"], unique = true)]
)
data class PersonLabel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val labelName: String,
    val colorCode: String = "#FF6B6B" // Default color (red)
)
