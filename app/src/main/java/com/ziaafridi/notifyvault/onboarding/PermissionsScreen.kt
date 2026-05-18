package com.ziaafridi.notifyvault.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
//import com.ziaafridi.notifyvault.isAppSideloaded

@Composable
fun PermissionsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var notificationPermissionGranted by remember {
        mutableStateOf(isNotificationServiceEnabled(context))
    }
    var autoStartPermissionGranted by remember {
        mutableStateOf(false)
    }
    var autoStartSettingsOpened by remember {
        mutableStateOf(false)
    }

    // Only show if they actually downloaded the APK via browser
//    val isSideloaded = remember { isAppSideloaded(context) }

    // Add a state to track if the user has attempted to enable at least once
//    var hasAttemptedEnable by remember { mutableStateOf(false) }

    // Check permissions when returning to the screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationPermissionGranted = isNotificationServiceEnabled(context)
                // If user has opened auto-start settings, assume they've enabled it
                if (autoStartSettingsOpened && !autoStartPermissionGranted) {
                    autoStartPermissionGranted = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Emoji
        Text(
            text = "🔔",
            fontSize = 64.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = stringResource(R.string.onboarding_permissions_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = stringResource(R.string.onboarding_permissions_description),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Permission Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notification Access Permission
            PermissionCard(
                title = stringResource(R.string.onboarding_notification_access),
                description = stringResource(R.string.onboarding_notification_description),
                isGranted = notificationPermissionGranted,
                onEnableClick = {
//                    hasAttemptedEnable = true // User has now clicked the button
                    openNotificationListenerSettings(context)
                }
            )

            // Restricted Settings Guidance Block
            // The device is Android 13 (API 33) or higher.
//            if (!notificationPermissionGranted && android.os.Build.VERSION.SDK_INT >= 33 && hasAttemptedEnable && isSideloaded) {
//                RestrictedSettingsGuidance()
//            }

            // Auto Start Permission (for Xiaomi and similar devices)
            if (isXiaomiDevice() || isHuaweiDevice() || isOppoDevice() || isVivoDevice() || isSamsungDevice()) {
                PermissionCard(
                    title = stringResource(R.string.onboarding_auto_start),
                    description = stringResource(R.string.onboarding_auto_start_description),
                    isGranted = autoStartPermissionGranted,
                    onEnableClick = {
                        autoStartSettingsOpened = true
                        openAutoStartSettings(context)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue Button
        Button(
            onClick = onComplete,
            enabled = notificationPermissionGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
               // .padding(top = 10.dp)
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (notificationPermissionGranted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (notificationPermissionGranted)
                    stringResource(R.string.onboarding_continue)
                else
                    stringResource(R.string.onboarding_enable_required_permissions),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation dots
        NavigationDots(currentStep = 1)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onEnableClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (isGranted) {
                Text(
                    text = stringResource(R.string.onboarding_enabled),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                OutlinedButton(
                    onClick = onEnableClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_enable),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

//@Composable
//fun RestrictedSettingsGuidance() {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
//        ),
//        border = androidx.compose.foundation.BorderStroke(
//            1.dp,
//            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
//        )
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "ℹ️",
//                    fontSize = 16.sp
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = stringResource(id = R.string.restricted_settings_title), // Fixed
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text(
//                text = stringResource(id = R.string.restricted_settings_desc), // Fixed
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Use ids of stringResource inside the list
//            val steps = listOf(
//                R.string.restricted_settings_step_1,
//                R.string.restricted_settings_step_2,
//                R.string.restricted_settings_step_3,
//                R.string.restricted_settings_step_4,
//                R.string.restricted_settings_step_5
//            )
//
//            steps.forEach { stepId ->
//                Text(
//                    // Removed the "• " prefix
//                    text = stringResource(stepId),
//                    fontSize = 12.sp,
//                    color = Color(0xFF1976D2),
//                    lineHeight = 16.sp,
//                    modifier = Modifier.padding(vertical = 1.dp)
//                )
//            }
//        }
//    }
//}



