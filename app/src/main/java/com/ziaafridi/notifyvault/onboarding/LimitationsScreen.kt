package com.ziaafridi.notifyvault.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.R
import com.ziaafridi.notifyvault.ui.theme.OnboardingGradients
import com.ziaafridi.notifyvault.ui.theme.onboardingMutedOnGradient
import com.ziaafridi.notifyvault.ui.theme.onboardingOnGradient

@Composable
fun LimitationsScreen(
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingGradients.deep())
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = stringResource(R.string.onboarding_limitations_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = onboardingOnGradient(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(40.dp))

        LimitationCard(text = stringResource(R.string.onboarding_limitations_offline))
        Spacer(modifier = Modifier.height(20.dp))
        LimitationCard(text = stringResource(R.string.onboarding_limitations_wifi))
        Spacer(modifier = Modifier.height(20.dp))
        RecommendationCard(text = stringResource(R.string.onboarding_limitations_recommendation))

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_finish),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        NavigationDots(currentStep = 6)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LimitationCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = onboardingOnGradient(),
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Composable
private fun RecommendationCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = onboardingOnGradient(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.onboarding_limitations_settings_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = onboardingMutedOnGradient(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
