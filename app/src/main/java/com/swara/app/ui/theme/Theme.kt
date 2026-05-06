package com.swara.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SignalNavy,
    onPrimary = Canvas,
    primaryContainer = SafetyOrangeSoft,
    onPrimaryContainer = Ink,
    secondary = Teal,
    onSecondary = Canvas,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    tertiary = SafetyOrange,
    onTertiary = Canvas,
    background = Canvas,
    onBackground = Ink,
    surface = WarmWhite,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = InkSoft,
    outline = StrokeLight
)

private val DarkColors = darkColorScheme(
    primary = TealLight,
    onPrimary = Ink,
    secondary = TealLight,
    onSecondary = Ink,
    tertiary = SafetyOrange,
    onTertiary = Ink,
    background = SignalNavyDeep,
    onBackground = WarmWhite,
    surface = NightSoft,
    onSurface = WarmWhite,
    surfaceVariant = NightLayer,
    onSurfaceVariant = WarmWhiteSoft,
    outline = StrokeDark
)

@Composable
fun SwaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
