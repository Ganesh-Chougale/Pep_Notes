package com.horizone.pep_notes.data.seed

import com.horizone.pep_notes.data.model.NoteLabel

/**
 * Default Note Labels that are pre-populated in the database.
 * These labels cannot be deleted or have their names changed by users.
 * Users can only add new labels with unique names.
 */
object DefaultNoteLabels {
    val defaultLabels = listOf(
        NoteLabel(id = 1, labelName = "Paid"),
        NoteLabel(id = 2, labelName = "Unpaid"),
        NoteLabel(id = 3, labelName = "advance"),
        NoteLabel(id = 4, labelName = "other"),
    )

    /**
     * Get all default label names (used for validation)
     */
    fun getDefaultLabelNames(): Set<String> = defaultLabels.map { it.labelName }.toSet()

    /**
     * Check if a label name is reserved (default)
     */
    fun isReservedLabelName(name: String): Boolean = getDefaultLabelNames().contains(name)
}
