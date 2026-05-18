package com.ziaafridi.notifyvault

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.ziaafridi.notifyvault.onboarding.getWhatsAppFolderUri
import com.ziaafridi.notifyvault.onboarding.hasMediaFolderAccess
import com.ziaafridi.notifyvault.onboarding.isNotificationServiceEnabled
import com.ziaafridi.notifyvault.ui.ServiceHealthBanner
import kotlinx.coroutines.launch
import androidx.core.content.FileProvider
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(navController: NavHostController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val messageRepository = remember { MessageRepository(context) }
    val scope = rememberCoroutineScope()

    // Add state to track Notification Listener status
    var isListenerActive by remember {
        mutableStateOf(isNotificationServiceEnabled(context))
    }

    // Refresh status whenever the user returns to the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isListenerActive = isNotificationServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val conversations =
        db.messageDao().getLatestConversations().collectAsState(initial = emptyList()).value
    // Fetch all messages that have media for Attachments
    val allMedia =
        db.messageDao().getAllMediaMessages().collectAsState(initial = emptyList()).value
    var hasMediaAccess by remember { mutableStateOf(hasMediaFolderAccess(context)) }
    var showMediaWarning by remember { mutableStateOf(!hasMediaAccess) }

    var isConversationSelectionMode by remember { mutableStateOf(false) }
    var selectedConversationIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteConversationDialog by remember { mutableStateOf(false) }

    var isAttachmentSelectionMode by remember { mutableStateOf(false) }
    var selectedAttachmentIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var visibleAttachmentIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTabIndex by remember { mutableStateOf(0) }

    // Folder picker launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Grant persistent permission to the selected folder
            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Store the URI for later use
            val prefs = context.getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("whatsapp_folder_uri", uri.toString()).apply()

            hasMediaAccess = true
            showMediaWarning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (isAttachmentSelectionMode) {
            TopAppBar(
                title = { Text("${selectedAttachmentIds.size}") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            isAttachmentSelectionMode = false
                            selectedAttachmentIds = emptySet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // select all visible
                            selectedAttachmentIds = visibleAttachmentIds.toSet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Select all"
                        )
                    }

                    IconButton(
                        onClick = {
                            val selected = allMedia.filter { selectedAttachmentIds.contains(it.id) }
                            if (selected.isEmpty()) return@IconButton
                            scope.launch {
                                messageRepository.deleteMessages(selected)
                                isAttachmentSelectionMode = false
                                selectedAttachmentIds = emptySet()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }

                    IconButton(
                        onClick = {
                            val selected = allMedia.filter { selectedAttachmentIds.contains(it.id) }
                            if (selected.isEmpty()) return@IconButton
                            shareMultiple(context, selected.mapNotNull { it.mediaPath })
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(
                        onClick = {
                            val selected = allMedia.filter { selectedAttachmentIds.contains(it.id) }
                            if (selected.isEmpty()) return@IconButton
                            scope.launch {
                                selected.mapNotNull { it.mediaPath }.forEach { path ->
                                    downloadToDownloads(context, path)
                                }
                                isAttachmentSelectionMode = false
                                selectedAttachmentIds = emptySet()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
                    }
                }
            )
        } else if (isConversationSelectionMode) {
            TopAppBar(
                title = { Text("${selectedConversationIds.size}") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            isConversationSelectionMode = false
                            selectedConversationIds = emptySet()
                            showDeleteConversationDialog = false
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedConversationIds =
                                conversations.map { it.conversationId }.distinct().toSet()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelectAll,
                            contentDescription = "Select all"
                        )
                    }
                    IconButton(
                        modifier = Modifier.padding(end = 10.dp),
                        onClick = {
                            if (selectedConversationIds.isEmpty()) return@IconButton
                            showDeleteConversationDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete chat",
                            tint = if (selectedConversationIds.isNotEmpty())
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            )
        } else {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.rwm_disp_name),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(24.dp),
                        onClick = { navController.navigate("settings") }
                    ) {
                        Icon(
                            painterResource(R.drawable.setting),
                            contentDescription = stringResource(R.string.setting_des),
                        )
                    }
                }
            )
        }

        // Add the Health Banner at the very top of the content column
        ServiceHealthBanner(
            isListenerActive = isListenerActive,
            onFixClick = {
                // Open the system settings for Notification Access
                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        )

        // Media access warning banner
        if (showMediaWarning && !hasMediaAccess) {
            MediaAccessWarningBanner(
                onGrantAccess = {
                    folderPickerLauncher.launch(getWhatsAppFolderUri(context))
                },
                onDismiss = { showMediaWarning = false }
            )
        }

        // Main content
        if (conversations.isEmpty()) {
            EmptyStateScreen()
        } else {
            RWDMTabBar(
                conversationList = conversations,
                // Attachments tab should show all media messages, not only the latest per conversation.
                mediaList = allMedia,
                navController = navController,
                onConversationLongPress = { conversationId ->
                    isConversationSelectionMode = true
                    isAttachmentSelectionMode = false
                    selectedAttachmentIds = emptySet()
                    selectedConversationIds = setOf(conversationId)
                    showDeleteConversationDialog = false
                },
                onConversationClick = { conversationId ->
                    if (isConversationSelectionMode) {
                        selectedConversationIds =
                            if (selectedConversationIds.contains(conversationId))
                                selectedConversationIds - conversationId
                            else
                                selectedConversationIds + conversationId
                        if (selectedConversationIds.isEmpty()) isConversationSelectionMode = false
                    }
                },
                isConversationSelectionMode = isConversationSelectionMode,
                selectedConversationIds = selectedConversationIds,
                isAttachmentSelectionMode = isAttachmentSelectionMode,
                selectedAttachmentIds = selectedAttachmentIds,
                onAttachmentClick = { msg ->
                    if (isAttachmentSelectionMode) {
                        selectedAttachmentIds =
                            if (selectedAttachmentIds.contains(msg.id))
                                selectedAttachmentIds - msg.id
                            else
                                selectedAttachmentIds + msg.id
                        if (selectedAttachmentIds.isEmpty()) isAttachmentSelectionMode = false
                    }
                },
                onAttachmentLongPress = { msg ->
                    isConversationSelectionMode = false
                    selectedConversationIds = emptySet()
                    isAttachmentSelectionMode = true
                    selectedAttachmentIds = setOf(msg.id)
                },
                onVisibleAttachmentIds = { ids -> visibleAttachmentIds = ids },
                onCurrentTabChanged = { idx -> currentTabIndex = idx },
            )
        }
    }

    if (showDeleteConversationDialog && selectedConversationIds.isNotEmpty() && isConversationSelectionMode) {
        AlertDialog(
            onDismissRequest = { showDeleteConversationDialog = false },
            title = { Text(stringResource(R.string.delete_conversation)) },
            text = { Text(stringResource(R.string.delete_conversation_warning)) },
            confirmButton = {
                Button(
                    onClick = {
                        val toDelete = selectedConversationIds.toList()
                        scope.launch {
                            toDelete.forEach { id ->
                                messageRepository.deleteConversation(id)
                            }
                            selectedConversationIds = emptySet()
                            isConversationSelectionMode = false
                            showDeleteConversationDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConversationDialog = false
                    }
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

private fun shareMultiple(context: Context, paths: List<String>) {
    runCatching {
        val uris = paths.mapNotNull { path ->
            val file = File(path)
            if (!file.exists()) return@mapNotNull null
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
        if (uris.isEmpty()) return
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}

private fun downloadToDownloads(context: Context, mediaPath: String): String? {
    return runCatching {
        val src = File(mediaPath)
        if (!src.exists()) return@runCatching null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, src.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "*/*")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri =
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return@runCatching null
            context.contentResolver.openOutputStream(uri)?.use { out ->
                FileInputStream(src).use { input ->
                    input.copyTo(out)
                }
            }
            "Downloads/${src.name}"
        } else null
    }.getOrNull()
}


@Composable
fun MediaAccessWarningBanner(
    onGrantAccess: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📂",
                fontSize = 39.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.media_access_not_granted),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.granted_media_access),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                OutlinedButton(
                    onClick = onGrantAccess,
                    modifier = Modifier
                        .height(32.dp)
                        .width(90.dp)
                        .padding(top = 5.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.grant),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))


//            Spacer(modifier = Modifier.width(8.dp))
//            IconButton(
//                onClick = onDismiss,
//                modifier = Modifier.size(24.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Dismiss",
//                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                    modifier = Modifier.size(16.dp)
//                )
//            }
        }
    }
}