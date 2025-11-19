package com.horizone.pep_notes.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithLabels(
    @Embedded
    val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteLabelCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "labelId"
        )
    )
    val labels: List<NoteLabel>
)
