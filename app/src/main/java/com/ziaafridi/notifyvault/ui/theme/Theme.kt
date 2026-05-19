package com.ziaafridi.notifyvault.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VaultIndigo,
    onPrimary = VaultOnPrimary,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = VaultIndigoDark,
    secondary = VaultTeal,
    onSecondary = VaultOnPrimary,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = Color(0xFF7C3AED),
    onTertiary = VaultOnPrimary,
    background = VaultSlate50,
    onBackground = VaultSlate900,
    surface = Color.White,
    onSurface = VaultSlate900,
    surfaceVariant = VaultSlate100,
    onSurfaceVariant = VaultSlate800,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = VaultError,
)

private val DarkColorScheme = darkColorScheme(
    primary = VaultIndigoLight,
    onPrimary = VaultIndigoDark,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = VaultTealLight,
    onSecondary = Color(0xFF134E4A),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF4C1D95),
    background = VaultSlate950,
    onBackground = VaultOnDark,
    surface = VaultSlate900,
    onSurface = VaultOnDark,
    surfaceVariant = VaultSlate800,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171),
)

@Composable
fun NotifyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
