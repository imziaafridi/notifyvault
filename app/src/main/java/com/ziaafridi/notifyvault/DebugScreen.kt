package com.ziaafridi.notifyvault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    
    val allMessages = db.messageDao().getLatestConversations().collectAsState(initial = emptyList()).value
    var messageCount by remember { mutableStateOf(0) }
    
    LaunchedEffect(allMessages) {
        messageCount = allMessages.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Database Status",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total conversations: $messageCount")
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    // Insert a test message
                                    val testMessage = RecoveredMessage(
                                        sender = "Test Sender",
                                        message = "Test message at ${System.currentTimeMillis()}",
                                        timestamp = System.currentTimeMillis(),
                                        conversationId = "test_conversation",
                                        conversationType = ConversationType.INDIVIDUAL,
                                        notificationKey = "test_${System.currentTimeMillis()}",
                                        messageHash = "test_hash_${System.currentTimeMillis()}"
                                    )
                                    db.messageDao().insert(testMessage)
                                }
                            }
                        ) {
                            Text("Insert Test Message")
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Notification Service Status",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Check logcat for 'WAMR' tags to see notification processing")
                        Text("Package filter: com.whatsapp*")
                    }
                }
            }
            
            if (allMessages.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Messages",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                
                items(allMessages.take(10)) { message ->
                    Card {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Sender: ${message.sender}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Message: ${message.message}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Conversation: ${message.conversationId}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Time: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(message.timestamp))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                item {
                    Card {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No messages found")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "1. Make sure notification access is granted\n2. Send a WhatsApp message\n3. Check logcat for 'WAMR' logs",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}