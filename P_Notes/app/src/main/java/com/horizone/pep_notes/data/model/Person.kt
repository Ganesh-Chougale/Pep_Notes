package com.horizone.pep_notes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
