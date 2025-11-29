package com.horizone.pep_notes.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_labels",
    indices = [Index(value = ["labelName", "colorCode"], unique = true)]
)
data class NoteLabel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val labelName: String,
    val colorCode: String = "#808080" // Default color (gray)
)
