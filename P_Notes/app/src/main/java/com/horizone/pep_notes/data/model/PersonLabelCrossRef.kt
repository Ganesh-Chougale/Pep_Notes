package com.horizone.pep_notes.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "person_label_cross_ref",
    primaryKeys = ["personId", "labelId"],
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonLabel::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("labelId")
    ]
)
data class PersonLabelCrossRef(
    val personId: Int,
    val labelId: Int
)
