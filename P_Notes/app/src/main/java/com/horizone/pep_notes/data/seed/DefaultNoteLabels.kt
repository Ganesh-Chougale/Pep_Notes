package com.horizone.pep_notes.data.seed

import com.horizone.pep_notes.data.model.NoteLabel

/**
 * Default Note Labels that are pre-populated in the database.
 * These labels cannot be deleted or have their names changed by users.
 * Users can only add new labels with unique names.
 */
object DefaultNoteLabels {
    val defaultLabels = listOf<NoteLabel>()

    /**
     * Get all default label names (used for validation)
     */
    fun getDefaultLabelNames(): Set<String> = defaultLabels.map { it.labelName }.toSet()

    /**
     * Get all default color codes (used for validation)
     */
    fun getDefaultColorCodes(): Set<String> = defaultLabels.map { it.colorCode }.toSet()

    /**
     * Check if a label name is reserved (default)
     */
    fun isReservedLabelName(name: String): Boolean = getDefaultLabelNames().contains(name)

    /**
     * Check if a color code is reserved (default)
     */
    fun isReservedColorCode(colorCode: String): Boolean = getDefaultColorCodes().contains(colorCode)
}
