package com.ziaafridi.notifyvault.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.MediaFolderManager

@Composable
fun MediaAccessBanner(
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val mediaFolderManager = remember { MediaFolderManager(context) }
    var hasAccess by remember { mutableStateOf(mediaFolderManager.hasWhatsAppFolderAccess()) }
    
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Grant persistent permission to the selected folder
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            mediaFolderManager.setWhatsAppFolderUri(uri)
            hasAccess = true
        }
    }
    
    if (!hasAccess) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder, // Using FolderOpen instead of Folder
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Enable Media Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Grant access to WhatsApp's media folder to view images, videos, and documents alongside messages.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { 
                            folderPickerLauncher.launch(null)
                        }
                    ) {
                        Text("Grant Access")
                    }
                }
            }
        }
    }
}

@Composable
fun MediaAccessDialog(
    onDismiss: () -> Unit,
    onGrantAccess: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Media Access Required",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "To view media files (images, videos, audio) in your recovered messages, please grant access to WhatsApp's media folder.\n\nThis allows the app to find and display media files that were sent in your conversations.",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onGrantAccess) {
                Text("Grant Access")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

/**
 * Helper function to check if WhatsApp is installed
 */
fun isWhatsAppInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo("com.whatsapp", 0)
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Helper function to get WhatsApp media folder suggestions
 */
fun getWhatsAppMediaFolderSuggestions(): List<String> {
    return listOf(
        "Android/media/com.whatsapp/WhatsApp",
        "WhatsApp/Media",
        "WhatsApp",
        "Android/media/whatsapp"
    )
}