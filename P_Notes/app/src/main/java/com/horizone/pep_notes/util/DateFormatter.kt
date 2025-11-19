package com.horizone.pep_notes.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateFormatter {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }

    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }
}
