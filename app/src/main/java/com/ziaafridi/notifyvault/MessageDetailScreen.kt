package com.ziaafridi.notifyvault

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ziaafridi.notifyvault.ui.DisplayDateBadge
import com.ziaafridi.notifyvault.ui.theme.VaultTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailScreen(conversationId: String, sender: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val messageRepository = remember { MessageRepository(context) }
    val scope = rememberCoroutineScope()

    val pageSize = 50
    val listState = rememberLazyListState()

    var isPageLoading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var messages by remember { mutableStateOf<List<RecoveredMessage>>(emptyList()) }
    var didInitialScroll by remember { mutableStateOf(false) }
    var pendingAnchorKey by remember { mutableStateOf<String?>(null) }
    var pendingAnchorOffsetPx by remember { mutableStateOf(0) }

    val tag = "MessageDetailPaging"

    // Unique key to prevent UI flickering and duplicate messages
    fun stableMessageKey(msg: RecoveredMessage): String {
        val sourceFromFileName =
            if (!msg.mediaFileName.isNullOrBlank() && !msg.mediaType.isNullOrBlank()) {
                "M|${msg.conversationId}|${msg.mediaType}|${msg.mediaFileName}"
            } else null

        val sourceFromPath = msg.mediaPath?.let { path ->
            runCatching {
                val fileNameNoExt = File(path).nameWithoutExtension
                val sourceBase = fileNameNoExt.replace(Regex("-\\d+$"), "")
                val mediaTypeKey = msg.mediaType ?: "UNKNOWN"
                "P|${msg.conversationId}|$mediaTypeKey|$sourceBase"
            }.getOrNull()
        }

        return sourceFromFileName ?: sourceFromPath ?: "N|${msg.notificationKey}"
    }

    // Build a single flat list of rows (date headers + messages) so indices match LazyListState.
    val rowItems by remember(messages) {
        derivedStateOf { buildRowItems(messages) }
    }

    suspend fun loadNextPage() {
        if (isPageLoading || endReached) return
        // Capture current scroll position to restore it after loading
        val anchorIndex = listState.firstVisibleItemIndex
        val anchorOffsetPx = listState.firstVisibleItemScrollOffset
        val anchorKey = rowItems.getOrNull(anchorIndex)?.let(::rowItemKey)

        isPageLoading = true
        val startMs = System.currentTimeMillis()
        Log.d(
            tag,
            "loadNextPage start convId=$conversationId offset=$offset pageSize=$pageSize anchorIndex=$anchorIndex anchorKey=$anchorKey"
        )
        try {
            val page = try {
                withContext(Dispatchers.IO) {
                    db.messageDao().getMessagesForConversationPaged(
                        conversationId = conversationId,
                        limit = pageSize,
                        offset = offset // tells db how many items been loaded
                    )
                }
            } catch (e: Exception) {
                Log.e(tag, "getMessagesForConversationPaged failed", e)
                emptyList()
            }

            if (page.isEmpty()) {
                endReached = true
                Log.d(tag, "loadNextPage got empty page -> endReached=true")
                return
            }

            offset += page.size
            messages = (messages + page).distinctBy(::stableMessageKey).sortedBy { it.timestamp }

            // Preserve scroll position after we rebuild rowItems.
            if (!anchorKey.isNullOrBlank()) {
                pendingAnchorKey = anchorKey
                pendingAnchorOffsetPx = anchorOffsetPx
            }
            if (page.size < pageSize) endReached = true
            Log.d(
                tag,
                "loadNextPage success loaded=${page.size} newOffset=$offset totalMessages=${messages.size} endReached=$endReached"
            )
        } finally {
            // Ensure loader is visible long enough
            val elapsed = System.currentTimeMillis() - startMs
            val minShowMs = 500L
            if (elapsed < minShowMs) delay(minShowMs - elapsed)
            isPageLoading = false
            Log.d(tag, "loadNextPage done elapsedMs=${System.currentTimeMillis() - startMs}")
        }
    }

    fun resetPaging() {
        isPageLoading = false
        endReached = false
        offset = 0
        messages = emptyList()
        didInitialScroll = false
        pendingAnchorKey = null
        pendingAnchorOffsetPx = 0
    }

    // Start fetching data when screen opens
    LaunchedEffect(conversationId) {
        resetPaging()
        loadNextPage()
    }

    // Trigger the next page load when the user scrolls to the top of the list
    LaunchedEffect(conversationId, listState) {

        var prevIndex = Int.MAX_VALUE
        var prevScrollOffset = Int.MAX_VALUE

        androidx.compose.runtime.snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .filter { didInitialScroll } // don't trigger during initial auto-scroll
            .collect { (idx, scrollOffset) ->
                val movedUp =
                    (idx < prevIndex) || (idx == prevIndex && scrollOffset < prevScrollOffset)

                // Triggered when user scrolls near the top (index <= 3)
                val nearTop = idx <= 3

                if (movedUp && nearTop && !isPageLoading && !endReached) {
                    Log.d(
                        tag,
                        "topTrigger fired idx=$idx prevIdx=$prevIndex offset=$offset isPageLoading=$isPageLoading endReached=$endReached"
                    )
                    loadNextPage()
                }

                prevIndex = idx
                prevScrollOffset = scrollOffset
            }
    }

    // After paging, restore the user's anchor row.
    LaunchedEffect(rowItems.size, pendingAnchorKey) {
        val key = pendingAnchorKey ?: return@LaunchedEffect
        val idx = rowItems.indexOfFirst { rowItemKey(it) == key }
        if (idx >= 0) {
            listState.scrollToItem(idx, pendingAnchorOffsetPx)
        }
        pendingAnchorKey = null
    }

    // On first load, jump to the bottom (latest message).
    LaunchedEffect(offset, isPageLoading, rowItems.size) {
        if (!didInitialScroll && rowItems.isNotEmpty() && offset > 0 && !isPageLoading) {
            listState.scrollToItem(rowItems.lastIndex.coerceAtLeast(0))
            didInitialScroll = true
            Log.d(tag, "initialScrollToBottom done rowItems=${rowItems.size} offset=$offset")
        }
    }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedMessages by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedMessages = emptySet()
        showDeleteDialog = false
    }

    suspend fun loadAllMessagesForSelectAll() {
        val all = withContext(Dispatchers.IO) {
            db.messageDao().getAllMessagesForConversation(conversationId)
        }.sortedBy { it.timestamp }

        messages = all.distinctBy(::stableMessageKey)
        offset = messages.size
        endReached = true
        didInitialScroll = true
        selectedMessages = messages.map { it.id }.toSet()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isPageLoading && messages.isNotEmpty()) Modifier.blur(2.dp) else Modifier)
        ) {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text("${selectedMessages.size} selected")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            exitSelectionMode()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.navigate_back_des)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    loadAllMessagesForSelectAll()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select all"
                            )
                        }

                        if (selectedMessages.isNotEmpty()) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete selected")
                            }
                        }
                    },
                    colors = VaultTheme.topAppBarSurfaceColors(),
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            sender,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = VaultTheme.topAppBarColors(),
                )
            }

            if (messages.isEmpty()) {
                EmptyStateScreen(message = "No messages from $sender yet.")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        //  .padding(15.dp)
                        .fillMaxSize()
                    //     .height(50.dp)
                    ,
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        // system navigation bar height + extra margin
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = rowItems,
                        key = { _, item -> rowItemKey(item) }
                    ) { _, item ->
                        when (item) {
                            is MessageRowItem.DateHeader -> DisplayDateBadge(item.label)
                            is MessageRowItem.Message -> MessageBubbleWithSelection(
                                message = item.message,
                                isSelected = selectedMessages.contains(item.message.id),
                                isSelectionMode = isSelectionMode,
                                onToggleSelection = {
                                    selectedMessages =
                                        if (selectedMessages.contains(item.message.id)) {
                                            selectedMessages - item.message.id
                                        } else {
                                            selectedMessages + item.message.id
                                        }
                                },
                                onLongPress = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedMessages = setOf(item.message.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        if (isPageLoading) {
            LoadingOverlay(modifier = Modifier.fillMaxSize())
        }
    }

    // Delete selected messages dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_messages)) },
            text = {
                Text(
                    "${stringResource(R.string.are_u_sure_to_delete)} ${selectedMessages.size} message${
                        if (selectedMessages.size > 1) stringResource(
                            R.string.s
                        ) else ""
                    }? ${stringResource(R.string.this_action_cannot_be_done)}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val messagesToDelete =
                                messages.filter { selectedMessages.contains(it.id) }
                            messageRepository.deleteMessages(messagesToDelete)
                            messages = messages.filterNot { selectedMessages.contains(it.id) }
                            offset = messages.size
                            if (messages.isEmpty()) {
                                exitSelectionMode()
                                onBack()
                                return@launch
                            }
                            exitSelectionMode()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}


@Composable
private fun MessageBubbleWithSelection(
    message: RecoveredMessage,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val contentAlpha = if (message.isDeleted) 0.6f else 1.0f
    val backgroundColor = VaultTheme.messageBubbleColor(
        isDeleted = message.isDeleted,
        isSelected = isSelected,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            //   .height(50.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    )
                )
                .background(backgroundColor)
                .combinedClickable(
                    onClick = {
                        if (isSelectionMode) onToggleSelection()
                    },
                    onLongClick = onLongPress
                )
                .padding(12.dp)
        ) {

            if (message.isDeleted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "This message was deleted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

//            // display media
//            if (!message.mediaPath.isNullOrEmpty() || !message.mediaType.isNullOrBlank() || !message.mediaFileName.isNullOrBlank()) {
//                com.ziaafridi.notifyvault.ui.MediaViewer(
//                    message = message,
//                    modifier = Modifier.padding(bottom = if (message.message.isNotBlank()) 8.dp else 0.dp)
//                )
//            }

            // Show text message if available
            if (message.message.isNotBlank()) {
                val isGroupMessage = message.conversationType == ConversationType.GROUP

                if (isGroupMessage) {
                    // Extract only the person's name
                    val memberName = MessageUtils.extractSenderFromGroupMessage(message.message)

                    if (memberName != null) {
                        Text(
                            text = "~ $memberName",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Extract only the message content
                    val actualContent = MessageUtils.extractContentFromGroupMessage(message.message)
                    Text(
                        text = actualContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isDeleted)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                } else {
                    // Individual chat just show the message
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isDeleted)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                    )
                }
            }

            Text(
                text = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date(message.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = if (message.isDeleted) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = contentAlpha
                ),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}


private sealed interface MessageRowItem {
    data class DateHeader(val label: String) : MessageRowItem
    data class Message(val message: RecoveredMessage) : MessageRowItem
}

private fun buildRowItems(messages: List<RecoveredMessage>): List<MessageRowItem> {
    if (messages.isEmpty()) return emptyList()

    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val rows = ArrayList<MessageRowItem>(messages.size + 8)

    var lastDate: String? = null
    for (msg in messages) {
        val dateLabel = dateFormat.format(Date(msg.timestamp))
        if (lastDate != dateLabel) {
            lastDate = dateLabel
            rows.add(MessageRowItem.DateHeader(dateLabel))
        }
        rows.add(MessageRowItem.Message(msg))
    }
    return rows
}

private fun rowItemKey(item: MessageRowItem): String {
    return when (item) {
        is MessageRowItem.DateHeader -> "H|${item.label}"
        is MessageRowItem.Message -> "M|${item.message.conversationId}|${item.message.notificationKey}"
    }
}

@Composable
private fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(18.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}