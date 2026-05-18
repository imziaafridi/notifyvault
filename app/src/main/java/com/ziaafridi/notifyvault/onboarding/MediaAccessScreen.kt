package com.ziaafridi.notifyvault.onboarding

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziaafridi.notifyvault.R

@Composable
fun MediaAccessScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var folderAccessGranted by remember { mutableStateOf(false) }
    val isWhatsAppInstalled = remember { isWhatsAppInstalled(context) }
    val scrollState = rememberScrollState()

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Grant persistent permission to the selected folder
            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            folderAccessGranted = true

            // Store the URI for later use
            val prefs = context.getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("whatsapp_folder_uri", uri.toString()).apply()
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

        // WhatsApp icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💬",
                fontSize = 60.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = stringResource(R.string.media_access_title_optional),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = stringResource(R.string.media_access_description_full),
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // WhatsApp access card
        WhatsAppAccessCard(
            folderAccessGranted = folderAccessGranted,
            isWhatsAppInstalled = isWhatsAppInstalled,
            onGrantAccess = {
                // Try to open WhatsApp folder directly using enhanced navigation
                try {
                    val success = openWhatsAppFolderInFileManager(context)
                    // Always launch the picker for permission granting, regardless of file manager success
                    folderPickerLauncher.launch(getWhatsAppFolderUri(context))
                } catch (e: Exception) {
                    // Fallback to standard document tree picker
                    folderPickerLauncher.launch(getWhatsAppFolderUri(context))
                }
            }
        )

        Spacer(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
        )

        // Continue Button - Always enabled now (optional access)
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (folderAccessGranted)
                    context.getString(R.string.onboarding_media_continue)
                else
                    stringResource(R.string.media_access_continue_without),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        // Skip button for users who don't want media access
        if (!folderAccessGranted) {
            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.media_access_skip_label),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
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
                            if (index == 4) Color.White else Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun WhatsAppAccessCard(
    folderAccessGranted: Boolean,
    isWhatsAppInstalled: Boolean,
    onGrantAccess: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF25D366), // green
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = context.getString(R.string.onboarding_media_whatsapp_access),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = if (folderAccessGranted)
                            context.getString(R.string.onboarding_media_access_description)
                        else
                            "We'll help you navigate to: ${getWhatsAppFolderInstructions(context)}",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            }

            // Show info about WhatsApp detection
            if (!isWhatsAppInstalled && !folderAccessGranted) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F8FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ℹ️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp not detected. You can still grant folder access if you have WhatsApp installed.",
                            fontSize = 12.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (!folderAccessGranted) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F8FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.media_access_step_title),
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2), // Blue 700
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            listOf(
                                R.string.media_access_step_1,
                                R.string.media_access_step_2,
                                R.string.media_access_step_3,
                                R.string.media_access_step_4
                            ).forEach { stepId ->
                                Text(
                                    text = stringResource(stepId),
                                    fontSize = 12.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Always enabled grant access btn
            Button(
                onClick = onGrantAccess,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (folderAccessGranted) Color(0xFF4CAF50) else Color(
                        0xFF00695C
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (folderAccessGranted)
                        context.getString(R.string.onboarding_media_access_granted)
                    else
                        context.getString(R.string.grant_folder_access)
                      //  "Grant Folder Access"
                    ,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}