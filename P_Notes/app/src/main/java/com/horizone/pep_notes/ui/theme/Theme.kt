package com.horizone.pep_notes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = ForestOnPrimaryDark,
    secondary = ForestSecondaryDark,
    onSecondary = ForestOnSecondaryDark,
    background = ForestBackgroundDark,
    onBackground = ForestOnBackgroundDark,
    surface = ForestSurfaceDark,
    onSurface = ForestOnSurfaceDark,
    surfaceVariant = ForestSurfaceVariantDark,
    onSurfaceVariant = ForestOnSurfaceVariantDark
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestPrimaryLight,
    onPrimary = ForestOnPrimaryLight,
    secondary = ForestSecondaryLight,
    onSecondary = ForestOnSecondaryLight,
    background = ForestBackgroundLight,
    onBackground = ForestOnBackgroundLight,
    surface = ForestSurfaceLight,
    onSurface = ForestOnSurfaceLight,
    surfaceVariant = ForestSurfaceVariantLight,
    onSurfaceVariant = ForestOnSurfaceVariantLight
)

@Composable
fun Pep_NotesTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        ForestDarkColorScheme
    } else {
        ForestLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}