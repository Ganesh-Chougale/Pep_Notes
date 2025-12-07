package com.horizone.pep_notes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = lightColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkPrimary,
    onSecondary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val SoothingColorScheme = lightColorScheme(
    primary = SoothingPrimary,
    onPrimary = SoothingOnPrimary,
    secondary = SoothingPrimary,
    onSecondary = SoothingOnPrimary,
    background = SoothingBackground,
    onBackground = SoothingOnBackground,
    surface = SoothingSurface,
    onSurface = SoothingOnSurface,
    surfaceVariant = SoothingSurfaceVariant,
    onSurfaceVariant = SoothingOnSurfaceVariant
)

private val OceanBlueColorScheme = lightColorScheme(
    primary = OceanBluePrimary,
    onPrimary = OceanBlueOnPrimary,
    secondary = OceanBluePrimary,
    onSecondary = OceanBlueOnPrimary,
    background = OceanBlueBackground,
    onBackground = OceanBlueOnBackground,
    surface = OceanBlueSurface,
    onSurface = OceanBlueOnSurface,
    surfaceVariant = OceanBlueSurfaceVariant,
    onSurfaceVariant = OceanBlueOnSurfaceVariant
)

private val SunsetOrangeColorScheme = lightColorScheme(
    primary = SunsetOrangePrimary,
    onPrimary = SunsetOrangeOnPrimary,
    secondary = SunsetOrangePrimary,
    onSecondary = SunsetOrangeOnPrimary,
    background = SunsetOrangeBackground,
    onBackground = SunsetOrangeOnBackground,
    surface = SunsetOrangeSurface,
    onSurface = SunsetOrangeOnSurface,
    surfaceVariant = SunsetOrangeSurfaceVariant,
    onSurfaceVariant = SunsetOrangeOnSurfaceVariant
)

private val ForestGreenColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = ForestGreenOnPrimary,
    secondary = ForestGreenPrimary,
    onSecondary = ForestGreenOnPrimary,
    background = ForestGreenBackground,
    onBackground = ForestGreenOnBackground,
    surface = ForestGreenSurface,
    onSurface = ForestGreenOnSurface,
    surfaceVariant = ForestGreenSurfaceVariant,
    onSurfaceVariant = ForestGreenOnSurfaceVariant
)

private val NeonPurpleColorScheme = lightColorScheme(
    primary = NeonPurplePrimary,
    onPrimary = NeonPurpleOnPrimary,
    secondary = NeonPurplePrimary,
    onSecondary = NeonPurpleOnPrimary,
    background = NeonPurpleBackground,
    onBackground = NeonPurpleOnBackground,
    surface = NeonPurpleSurface,
    onSurface = NeonPurpleOnSurface,
    surfaceVariant = NeonPurpleSurfaceVariant,
    onSurfaceVariant = NeonPurpleOnSurfaceVariant
)

private val GlassColorScheme = lightColorScheme(
    primary = GlassPrimary,
    onPrimary = GlassOnPrimary,
    secondary = GlassPrimary,
    onSecondary = GlassOnPrimary,
    background = GlassBackground,
    onBackground = GlassOnBackground,
    surface = GlassSurface,
    onSurface = GlassOnSurface,
    surfaceVariant = GlassSurfaceVariant,
    onSurfaceVariant = GlassOnSurfaceVariant
)

@Composable
fun pepTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun Pep_NotesTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.GLASS -> GlassColorScheme
        AppTheme.DARK -> DarkColorScheme
        AppTheme.SOOTHING -> SoothingColorScheme
        AppTheme.OCEAN_BLUE -> OceanBlueColorScheme
        AppTheme.SUNSET_ORANGE -> SunsetOrangeColorScheme
        AppTheme.FOREST_GREEN -> ForestGreenColorScheme
        AppTheme.NEON_PURPLE -> NeonPurpleColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}