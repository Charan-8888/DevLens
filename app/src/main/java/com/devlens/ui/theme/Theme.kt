package com.devlens.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DevLensDarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = TextPrimary,
    primaryContainer = VioletDark,
    onPrimaryContainer = VioletLight,
    secondary = Green,
    onSecondary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkPanel,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextMuted,
    outline = DarkLine,
    error = Red,
    onError = TextPrimary
)

@Composable
fun DevLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DevLensDarkColorScheme,
        typography = DevLensTypography,
        content = content
    )
}
