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

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanPrimaryDark,
    onPrimary = OceanOnPrimaryDark,
    secondary = OceanSecondaryDark,
    onSecondary = OceanOnSecondaryDark,
    background = OceanBackgroundDark,
    onBackground = OceanOnBackgroundDark,
    surface = OceanSurfaceDark,
    onSurface = OceanOnSurfaceDark,
    surfaceVariant = OceanSurfaceVariantDark,
    onSurfaceVariant = OceanOnSurfaceVariantDark
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimaryLight,
    onPrimary = OceanOnPrimaryLight,
    secondary = OceanSecondaryLight,
    onSecondary = OceanOnSecondaryLight,
    background = OceanBackgroundLight,
    onBackground = OceanOnBackgroundLight,
    surface = OceanSurfaceLight,
    onSurface = OceanOnSurfaceLight,
    surfaceVariant = OceanSurfaceVariantLight,
    onSurfaceVariant = OceanOnSurfaceVariantLight
)

private val DesertDarkColorScheme = darkColorScheme(
    primary = DesertPrimaryDark,
    onPrimary = DesertOnPrimaryDark,
    secondary = DesertSecondaryDark,
    onSecondary = DesertOnSecondaryDark,
    background = DesertBackgroundDark,
    onBackground = DesertOnBackgroundDark,
    surface = DesertSurfaceDark,
    onSurface = DesertOnSurfaceDark,
    surfaceVariant = DesertSurfaceVariantDark,
    onSurfaceVariant = DesertOnSurfaceVariantDark
)

private val DesertLightColorScheme = lightColorScheme(
    primary = DesertPrimaryLight,
    onPrimary = DesertOnPrimaryLight,
    secondary = DesertSecondaryLight,
    onSecondary = DesertOnSecondaryLight,
    background = DesertBackgroundLight,
    onBackground = DesertOnBackgroundLight,
    surface = DesertSurfaceLight,
    onSurface = DesertOnSurfaceLight,
    surfaceVariant = DesertSurfaceVariantLight,
    onSurfaceVariant = DesertOnSurfaceVariantLight
)

private val GothamDarkColorScheme = darkColorScheme(
    primary = GothamPrimaryDark,
    onPrimary = GothamOnPrimaryDark,
    secondary = GothamSecondaryDark,
    onSecondary = GothamOnSecondaryDark,
    background = GothamBackgroundDark,
    onBackground = GothamOnBackgroundDark,
    surface = GothamSurfaceDark,
    onSurface = GothamOnSurfaceDark,
    surfaceVariant = GothamSurfaceVariantDark,
    onSurfaceVariant = GothamOnSurfaceVariantDark,
    outline = GothamOutlineDark,
    outlineVariant = GothamOutlineVariantDark,
    onSecondaryContainer = GothamOnSecondaryDark,
    onTertiary = GothamOnSecondaryDark
)

private val GothamLightColorScheme = lightColorScheme(
    primary = GothamPrimaryLight,
    onPrimary = GothamOnPrimaryLight,
    secondary = GothamSecondaryLight,
    onSecondary = GothamOnSecondaryLight,
    background = GothamBackgroundLight,
    onBackground = GothamOnBackgroundLight,
    surface = GothamSurfaceLight,
    onSurface = GothamOnSurfaceLight,
    surfaceVariant = GothamSurfaceVariantLight,
    onSurfaceVariant = GothamOnSurfaceVariantLight
)

@Composable
fun Pep_NotesTheme(
    appTheme: AppTheme,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.FOREST -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        AppTheme.OCEAN -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        AppTheme.DESERT -> if (darkTheme) DesertDarkColorScheme else DesertLightColorScheme
        AppTheme.GOTHAM -> if (darkTheme) GothamDarkColorScheme else GothamLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}