//package com.ziaafridi.notifyvault
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.DropdownMenu
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.ziaafridi.notifyvault.ui.MediaAccessBanner
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ConversationListScreen(
//    onConversationClick: (String, String) -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getDatabase(context)
//    val messageRepository = remember { MessageRepository(context) }
//    val scope = rememberCoroutineScope()
//
//    val conversations =
//        db.messageDao().getLatestConversations().collectAsState(initial = emptyList()).value
//
//    var showDeleteDialog by remember { mutableStateOf(false) }
//    var conversationToDelete by remember { mutableStateOf<RecoveredMessage?>(null) }
//    var showMediaBanner by remember { mutableStateOf(true) }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Recovered Messages") },
//                actions = {
//                    IconButton(onClick = { /* Settings */ }) {
//                        Icon(Icons.Default.Settings, contentDescription = "Settings")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            contentPadding = PaddingValues(vertical = 8.dp)
//        ) {
//            // Media access banner
//            if (showMediaBanner) {
//                item {
//                    MediaAccessBanner(
//                        onDismiss = { showMediaBanner = false }
//                    )
//                }
//            }
//
//            if (conversations.isEmpty()) {
//                item {
//                    EmptyConversationsState()
//                }
//            } else {
//                items(conversations) { conversation ->
//                    ConversationItem(
//                        conversation = conversation,
//                        onClick = {
//                            val displayName = getConversationDisplayName(conversation)
//                            onConversationClick(conversation.conversationId, displayName)
//                        },
//                        onDeleteClick = {
//                            conversationToDelete = conversation
//                            showDeleteDialog = true
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    // Delete conversation dialog
//    if (showDeleteDialog && conversationToDelete != null) {
//        AlertDialog(
//            onDismissRequest = {
//                showDeleteDialog = false
//                conversationToDelete = null
//            },
//            title = { Text("Delete Conversation") },
//            text = {
//                Text(
//                    "Are you sure you want to delete the conversation with ${
//                        getConversationDisplayName(
//                            conversationToDelete!!
//                        )
//                    }? All messages and media will be permanently removed."
//                )
//            },
//            confirmButton = {
//                Button(
//                    onClick = {
//                        scope.launch {
//                            conversationToDelete?.let { conversation ->
//                                messageRepository.deleteConversation(conversation.conversationId)
//                            }
//                            showDeleteDialog = false
//                            conversationToDelete = null
//                        }
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//                ) {
//                    Text("Delete")
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = {
//                    showDeleteDialog = false
//                    conversationToDelete = null
//                }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//}
//
//@Composable
//private fun ConversationItem(
//    conversation: RecoveredMessage,
//    onClick: () -> Unit,
//    onDeleteClick: () -> Unit
//) {
//    var showMenu by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 4.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Avatar
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .background(
//                        getAvatarColor(conversation.conversationId),
//                        CircleShape
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = getAvatarText(conversation),
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 18.sp
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Conversation info
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = getConversationDisplayName(conversation),
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Spacer(modifier = Modifier.height(2.dp))
//
//                Text(
//                    text = MessageUtils.getMessagePreview(conversation),
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = if (conversation.isDeleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            // Timestamp and menu
//            Column(
//                horizontalAlignment = Alignment.End
//            ) {
//                Text(
//                    text = formatConversationTime(conversation.timestamp),
//                    style = MaterialTheme.typography.labelSmall,
//                    color = if (conversation.isDeleted) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Box {
//                    IconButton(
//                        onClick = { showMenu = true },
//                        modifier = Modifier.size(24.dp)
//                    ) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "More options",
//                            modifier = Modifier.size(16.dp)
//                        )
//                    }
//
//                    DropdownMenu(
//                        expanded = showMenu,
//                        onDismissRequest = { showMenu = false }
//                    ) {
//                        DropdownMenuItem(
//                            text = { Text("Delete conversation") },
//                            onClick = {
//                                showMenu = false
//                                onDeleteClick()
//                            },
//                            leadingIcon = {
//                                Icon(Icons.Default.Delete, contentDescription = null)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun EmptyConversationsState() {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(32.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "💬",
//                fontSize = 64.sp
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "No conversations yet",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Deleted WhatsApp messages will appear here when they're recovered",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = androidx.compose.ui.text.style.TextAlign.Center
//            )
//        }
//    }
//}
//
///**
// * Gets clean display name for conversation
// */
//private fun getConversationDisplayName(conversation: RecoveredMessage): String {
//    return when (conversation.conversationType) {
//        ConversationType.GROUP -> {
//            // Extract group name from conversation ID
//            val groupName = conversation.conversationId.removePrefix("group_")
//            MessageUtils.sanitizeConversationName(groupName)
//        }
//
//        ConversationType.INDIVIDUAL -> {
//            conversation.sender
//        }
//    }
//}
//
///**
// * Gets avatar text (first letter of name)
// */
//private fun getAvatarText(conversation: RecoveredMessage): String {
//    val displayName = getConversationDisplayName(conversation)
//    return displayName.firstOrNull()?.uppercase() ?: "?"
//}
//
///**
// * Gets avatar color based on conversation ID
// */
//private fun getAvatarColor(conversationId: String): Color {
//    val colors = listOf(
//        Color(0xFF1976D2), // Blue
//        Color(0xFF388E3C), // Green
//        Color(0xFFF57C00), // Orange
//        Color(0xFF7B1FA2), // Purple
//        Color(0xFFD32F2F), // Red
//        Color(0xFF0097A7), // Cyan
//        Color(0xFF5D4037), // Brown
//        Color(0xFF455A64)  // Blue Grey
//    )
//
//    val hash = conversationId.hashCode()
//    val index = kotlin.math.abs(hash) % colors.size
//    return colors[index]
//}
//
///**
// * Formats timestamp for conversation list
// */
//private fun formatConversationTime(timestamp: Long): String {
//    val now = System.currentTimeMillis()
//    val diff = now - timestamp
//
//    return when {
//        diff < 24 * 60 * 60 * 1000 -> { // Less than 24 hours
//            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
//        }
//
//        diff < 7 * 24 * 60 * 60 * 1000 -> { // Less than 7 days
//            SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
//        }
//
//        else -> {
//            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
//        }
//    }
//}