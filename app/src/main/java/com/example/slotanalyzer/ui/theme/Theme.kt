package com.example.slotanalyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SlateBlueLight,
    onPrimary = Color.Black,
    primaryContainer = SlateBlue,
    onPrimaryContainer = DarkTextPrimary,
    secondary = TealAccentLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF213A39),
    onSecondaryContainer = Color(0xFFD6FFF9),
    tertiary = Color(0xFFFFB7C8),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4B2534),
    onTertiaryContainer = Color(0xFFFFD9E2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = Color(0xFFE6EAF2),
    inverseOnSurface = Color(0xFF2B313C),
    inversePrimary = SlateBlue,
    scrim = Color(0xCC000000)
)

private val LightColorScheme = lightColorScheme(
    primary = SlateBlue,
    onPrimary = Color.White,
    primaryContainer = SlateBlueLight,
    onPrimaryContainer = Color(0xFF10205F),
    secondary = Color(0xFF006B5E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9DF2E2),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF8A3D57),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3A1220),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = Color(0xFF2A313C),
    inverseOnSurface = Color(0xFFF1F4F9),
    inversePrimary = SlateBlueLight,
    scrim = Color(0x99000000)
)

@Composable
fun SlotSettingAnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
