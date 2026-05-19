package com.ziaafridi.notifyvault.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shared Material 3 defaults inspired by WhatsApp — use instead of one-off colors in screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
object VaultTheme {

    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
    )

    /** Selection / contextual top bar (attachment picker, etc.) */
    @Composable
    fun topAppBarSurfaceColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )

    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    )

    @Composable
    fun textButtonColors() = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.primary,
    )

    @Composable
    fun elevatedCardColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )

    @Composable
    fun settingsCardColors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
    )

    @Composable
    fun settingsCardElevation() = CardDefaults.cardElevation(defaultElevation = 0.dp)

    @Composable
    fun tabRowContainerColor(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun tabRowIndicatorColor(): Color = MaterialTheme.colorScheme.primary

    @Composable
    fun messageBubbleColor(isDeleted: Boolean, isSelected: Boolean): Color = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isDeleted -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    @Composable
    fun textFieldColors() = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
    )
}
