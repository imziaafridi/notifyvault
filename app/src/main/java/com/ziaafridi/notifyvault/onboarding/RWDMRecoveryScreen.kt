package com.ziaafridi.notifyvault.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun RWDMRecoveryScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var showNotificationDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var notificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Permission not needed for older versions
            }
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        showNotificationDialog = false
        if (isGranted) {
            onComplete()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00695C))
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Receive a message
        Text(
            text = context.getString(R.string.onboarding_rwdm_step1),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Message notification card
        MessageNotificationCard(
            isDeleted = false,
            message = context.getString(R.string.onboarding_placeholder_message)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Message gets deleted
        Text(
            text = context.getString(R.string.onboarding_rwdm_step2),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Deleted message notification card
        MessageNotificationCard(
            isDeleted = true,
            message = stringResource(R.string.onboarding_recovery_message_deleted)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // rwdm recovers it
        Text(
            text = context.getString(R.string.onboarding_rwdm_step3),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // RWDM recovery notification card
        RWDMRecoveryCard()

        Spacer(modifier = Modifier.weight(1f))

        // Grant permission button
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showNotificationDialog = true
                } else {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = context.getString(R.string.onboarding_rwdm_grant_permission),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(7) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index == 2) Color.White else Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Notification permission dialog
    if (showNotificationDialog) {
        NotificationPermissionDialog(
            onAllow = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDontAllow = {
                showNotificationDialog = false
                onComplete()
            },
            onDismiss = { showNotificationDialog = false }
        )
    }
}

@Composable
private fun MessageNotificationCard(
    isDeleted: Boolean,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleted) Color.White.copy(alpha = 0.7f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat icon
            Text(
                text = "💬",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_placeholder_chat),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.onboarding_notification_suffix),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = stringResource(R.string.onboarding_placeholder_user),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = if (isDeleted) Color.Gray else Color.Black,
                    fontStyle = if (isDeleted) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                )
            }

            // Profile picture placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, RoundedCornerShape(20.dp))
            )
        }
    }
}

@Composable
private fun RWDMRecoveryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RWDM icon
            Text(
                text = "🔍",
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_placeholder_rwdm),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = stringResource(R.string.onboarding_time_now),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = stringResource(R.string.onboarding_recovery_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.onboarding_placeholder_message),
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionDialog(
    onAllow: () -> Unit,
    onDontAllow: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2E2E2E),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔔",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = context.getString(R.string.onboarding_rwdm_notification_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.onboarding_rwdm_allow),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDontAllow,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C6C6C)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = context.getString(R.string.onboarding_rwdm_dont_allow),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    )
}