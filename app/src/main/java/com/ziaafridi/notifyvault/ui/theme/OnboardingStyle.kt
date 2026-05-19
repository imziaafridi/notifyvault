package com.ziaafridi.notifyvault.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object OnboardingGradients {

    @Composable
    fun primary(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.verticalGradient(
            colors = listOf(
                scheme.primary,
                scheme.primaryContainer.copy(alpha = 0.85f),
            ),
        )
    }

    @Composable
    fun accent(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.verticalGradient(
            colors = listOf(
                scheme.secondary,
                scheme.secondaryContainer.copy(alpha = 0.9f),
            ),
        )
    }

    @Composable
    fun deep(): Brush {
        val scheme = MaterialTheme.colorScheme
        return Brush.verticalGradient(
            colors = listOf(
                scheme.tertiary.copy(alpha = 0.95f),
                scheme.primary.copy(alpha = 0.9f),
            ),
        )
    }
}

@Composable
fun onboardingOnGradient(): Color = Color.White

@Composable
fun onboardingMutedOnGradient(): Color = Color.White.copy(alpha = 0.85f)
