package com.ziaafridi.notifyvault

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.ui.MediaViewer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    conversationName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val messageRepository = remember { MessageRepository(context) }
    val scope = rememberCoroutineScope()

    val messages = db.messageDao().getMessagesForConversation(conversationId)
        .collectAsState(initial = emptyList()).value
        .distinctBy { msg ->
            val sourceFromFileName =
                if (!msg.mediaFileName.isNullOrBlank() && !msg.mediaType.isNullOrBlank()) {
                    "M|${msg.conversationId}|${msg.mediaType}|${msg.mediaFileName}"
                } else null

            val sourceFromPath = msg.mediaPath?.let { path ->
                runCatching {
                    val fileNameNoExt = java.io.File(path).nameWithoutExtension
                    val sourceBase = fileNameNoExt.replace(Regex("-\\d+$"), "")
                    val mediaTypeKey = msg.mediaType ?: "UNKNOWN"
                    "P|${msg.conversationId}|$mediaTypeKey|$sourceBase"
                }.getOrNull()
            }

            sourceFromFileName ?: sourceFromPath ?: "N|${msg.notificationKey}"
        }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedMessages by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteConversationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text("${selectedMessages.size} selected")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSelectionMode = false
                            selectedMessages = setOf()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (selectedMessages.isNotEmpty()) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = conversationName,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteConversationDialog = true }) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Delete conversation"
                            ) // Using DeleteForever instead of DeleteSweep
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            EmptyStateScreen(message = "No messages in this conversation yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        message = msg,
                        isSelected = selectedMessages.contains(msg.id),
                        isSelectionMode = isSelectionMode,
                        onToggleSelection = {
                            selectedMessages = if (selectedMessages.contains(msg.id)) {
                                selectedMessages - msg.id
                            } else {
                                selectedMessages + msg.id
                            }
                        },
                        onLongPress = {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedMessages = setOf(msg.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete selected messages dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Messages") },
            text = {
                Text("Are you sure you want to delete ${selectedMessages.size} message${if (selectedMessages.size > 1) "s" else ""}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val messagesToDelete =
                                messages.filter { selectedMessages.contains(it.id) }
                            messageRepository.deleteMessages(messagesToDelete)
                            selectedMessages = setOf()
                            isSelectionMode = false
                            showDeleteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete conversation dialog
    if (showDeleteConversationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConversationDialog = false },
            title = { Text("Delete Conversation") },
            text = {
                Text("Are you sure you want to delete this entire conversation? All messages and media will be permanently removed. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            messageRepository.deleteConversation(conversationId)
                            showDeleteConversationDialog = false
                            onBack() // Go back since conversation is deleted
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConversationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: RecoveredMessage,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Selection checkbox
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(end = 8.dp, top = 4.dp)
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = if (isSelectionMode) 240.dp else 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onToggleSelection()
                    },
                    onLongClick = onLongPress
                )
                .padding(12.dp)
        ) {
            val isGroupMessage = message.conversationType == ConversationType.GROUP
            if (isGroupMessage) {
                val memberName = MessageUtils.extractSenderFromGroupMessage(message.message)
                if (!memberName.isNullOrBlank()) {
                    Text(
                        text = "~ $memberName",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Show media if available
            if (message.mediaPath != null || message.mediaType != null) {
                MediaViewer(
                    message = message,
                    modifier = Modifier.padding(bottom = if (message.message.isNotBlank()) 8.dp else 0.dp)
                )
            }

            // Show text message if available
            if (message.message.isNotBlank()) {
                val displayText = if (isGroupMessage) {
                    MessageUtils.extractContentFromGroupMessage(message.message)
                } else {
                    message.message
                }
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Timestamp
            Text(
                text = SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}
