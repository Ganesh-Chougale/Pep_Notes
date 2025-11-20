package com.horizone.pep_notes.data.seed

import com.horizone.pep_notes.data.model.PersonLabel

/**
 * Default Person Labels that are pre-populated in the database.
 * These labels cannot be deleted or have their names/colors changed by users.
 * Users can only add new labels with unique names and colors.
 */
object DefaultPersonLabels {
    val defaultLabels = listOf(
        PersonLabel(id = 1, labelName = "Family", colorCode = "#FF6B6B"),      // Red
        PersonLabel(id = 2, labelName = "Friend", colorCode = "#4ECDC4"),      // Teal
        PersonLabel(id = 3, labelName = "Colleague", colorCode = "#45B7D1"),   // Blue
        PersonLabel(id = 4, labelName = "VIP", colorCode = "#FFA07A"),         // Light Salmon
        PersonLabel(id = 5, labelName = "Important", colorCode = "#FFD93D"),   // Yellow
        PersonLabel(id = 6, labelName = "Follow Up", colorCode = "#6BCB77"),   // Green
        PersonLabel(id = 7, labelName = "Mentor", colorCode = "#9D84B7"),      // Purple
        PersonLabel(id = 8, labelName = "Client", colorCode = "#FF8B94")       // Pink
    )

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
