package com.ziaafridi.notifyvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WaGreen,
    onPrimary = WaOnPrimary,
    primaryContainer = WaBubbleOutgoing,
    onPrimaryContainer = WaOnSurfaceLight,
    secondary = WaGreenLight,
    onSecondary = WaOnPrimary,
    secondaryContainer = WaBubbleOutgoing,
    onSecondaryContainer = WaOnSurfaceLight,
    tertiary = WaLink,
    onTertiary = WaOnPrimary,
    background = WaBackgroundLight,
    onBackground = WaOnSurfaceLight,
    surface = WaSurfaceLight,
    onSurface = WaOnSurfaceLight,
    surfaceVariant = WaSurfaceVariantLight,
    onSurfaceVariant = WaOnSurfaceVariantLight,
    outline = WaOutlineLight,
    outlineVariant = WaOutlineLight,
    error = WaError,
    onError = WaOnPrimary,
    errorContainer = Color(0xFFFFE5E9),
    onErrorContainer = WaError,
)

private val DarkColorScheme = darkColorScheme(
    primary = WaGreenDarkMode,
    onPrimary = WaOnSurfaceDark,
    primaryContainer = WaBubbleOutgoingDark,
    onPrimaryContainer = WaOnSurfaceDark,
    secondary = WaGreenDarkMode,
    onSecondary = WaBackgroundDark,
    secondaryContainer = WaGreenDarkContainer,
    onSecondaryContainer = WaOnSurfaceDark,
    tertiary = WaLink,
    onTertiary = WaOnSurfaceDark,
    background = WaBackgroundDark,
    onBackground = WaOnSurfaceDark,
    surface = WaSurfaceDark,
    onSurface = WaOnSurfaceDark,
    surfaceVariant = WaSurfaceVariantDark,
    onSurfaceVariant = WaOnSurfaceVariantDark,
    outline = WaOutlineDark,
    outlineVariant = WaOutlineDark,
    error = Color(0xFFF15C6D),
    onError = WaOnSurfaceDark,
    errorContainer = Color(0xFF5C1A24),
    onErrorContainer = Color(0xFFF15C6D),
)

@Composable
fun NotifyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = VaultShapes,
        content = content,
    )
}
