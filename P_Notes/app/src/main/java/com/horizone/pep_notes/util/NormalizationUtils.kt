package com.horizone.pep_notes.util

import java.text.Normalizer
import java.util.Locale

private val ZERO_WIDTH_REGEX = Regex("[\u200B\u200C\u200D\uFEFF\u00AD]")
private val WHITESPACE_REGEX = Regex("\\s+")

fun normalizeLabelName(name: String): String {
    var s = ZERO_WIDTH_REGEX.replace(name, "")
    s = Normalizer.normalize(s, Normalizer.Form.NFC)
    s = WHITESPACE_REGEX.replace(s.trim(), " ")
    return s.lowercase(Locale.ROOT)
}

fun normalizeColor(code: String?, defaultColor: String): String {
    fun canonicalOrNull(input: String): String? {
        var t = input.trim()
        if (t.startsWith("#")) t = t.substring(1)
        if (t.length == 3) {
            if (!t.matches(Regex("[0-9a-fA-F]{3}"))) return null
            val r = t[0]
            val g = t[1]
            val b = t[2]
            t = "${r}${r}${g}${g}${b}${b}"
        }
        if (t.length == 6 || t.length == 8) {
            if (!t.matches(Regex("[0-9a-fA-F]{${t.length}}"))) return null
            return "#" + t.uppercase(Locale.ROOT)
        }
        return null
    }

    val normalized = code?.let { canonicalOrNull(it) }
    if (normalized != null) return normalized

    return canonicalOrNull(defaultColor) ?: "#808080"
}
