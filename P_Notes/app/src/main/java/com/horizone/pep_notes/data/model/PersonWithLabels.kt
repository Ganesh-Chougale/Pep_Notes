package com.horizone.pep_notes.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PersonWithLabels(
    @Embedded
    val person: Person,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = PersonLabelCrossRef::class,
            parentColumn = "personId",
            entityColumn = "labelId"
        )
    )
    val labels: List<PersonLabel>
)
