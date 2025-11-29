package com.horizone.pep_notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizationUtilsTest {

    @Test
    fun normalizeLabelName_trims_collapses_whitespace_and_casefolds() {
        val input = "  PeP\t Notes   Label  "
        val expected = "pep notes label"
        assertEquals(expected, normalizeLabelName(input))
    }

    @Test
    fun normalizeLabelName_removes_zero_width_and_normalizes_nfc() {
        val zwsp = "\u200B"
        val nfd = "e\u0301"
        val input = " L$zwsp$nfd bel "
        val expected = "lé bel".lowercase()
        assertEquals(expected, normalizeLabelName(input))
    }

    @Test
    fun normalizeColor_accepts_plain_hex6_and_uppercases() {
        assertEquals("#FF6B6B", normalizeColor("ff6b6b", "#808080"))
        assertEquals("#808080", normalizeColor("#808080", "#FF6B6B"))
    }

    @Test
    fun normalizeColor_expands_hex3() {
        assertEquals("#FF66AA", normalizeColor("#f6a", "#808080"))
        assertEquals("#112233", normalizeColor("123", "#808080"))
    }

    @Test
    fun normalizeColor_accepts_hex8() {
        assertEquals("#11223344", normalizeColor("11223344", "#808080"))
        assertEquals("#AABBCCDD", normalizeColor("#aabbccdd", "#FF6B6B"))
    }

    @Test
    fun normalizeColor_invalid_returns_default_canonical_or_gray() {
        assertEquals("#FF6B6B", normalizeColor("not-a-color", "#ff6b6b"))
        assertEquals("#808080", normalizeColor(null, "#808080"))
        assertEquals("#808080", normalizeColor("not-a-color", "invalid-default"))
    }
}

