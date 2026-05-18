package com.ziaafridi.notifyvault

import androidx.compose.runtime.*
import com.ziaafridi.notifyvault.onboarding.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    when (currentStep) {
        0 -> TermsAndConditionsScreen(
            onAccept = { currentStep = 1 }
        )
        1 -> PermissionsScreen(
            onComplete = { currentStep = 2 }
        )
        2 -> RWDMRecoveryScreen(
            onComplete = { currentStep = 3 }
        )
        3 -> RememberScreen(
            onComplete = { currentStep = 4 }
        )
        4 -> MediaAccessScreen(
            onComplete = { currentStep = 5 }
        )
        5 -> PrivacyScreen(
            onComplete = { currentStep = 6 }
        )
        6 -> LimitationsScreen(
            onComplete = onComplete
        )
    }
}