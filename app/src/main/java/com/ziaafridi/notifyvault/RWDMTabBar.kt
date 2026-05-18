package com.ziaafridi.notifyvault

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ziaafridi.notifyvault.ui.DisplayDateBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RWDMTabBar(
    conversationList: List<RecoveredMessage>,
    mediaList: List<RecoveredMessage>,
    navController: NavHostController,
    onConversationLongPress: (conversationId: String) -> Unit,
    onConversationClick: (conversationId: String) -> Unit,
    isConversationSelectionMode: Boolean,
    selectedConversationIds: Set<String>,
    isAttachmentSelectionMode: Boolean,
    selectedAttachmentIds: Set<Int>,
    onAttachmentClick: (RecoveredMessage) -> Unit,
    onAttachmentLongPress: (RecoveredMessage) -> Unit,
    onVisibleAttachmentIds: (List<Int>) -> Unit,
    onCurrentTabChanged: (Int) -> Unit,
) {
    val tabs = listOf("Notifications", "Attachments")
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState {
        tabs.size
    }

    // Only show media that was actually deleted
    val deletedMediaList = remember(mediaList) {
        mediaList.filter { it.isDeleted }
    }

    // Group media items by date (off main thread to reduce resume jank)
    var groupedMessages by remember { mutableStateOf<Map<String, List<RecoveredMessage>>>(emptyMap()) }
    var visibleAttachmentIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(deletedMediaList) {
        val (grouped, visibleIds) = withContext(Dispatchers.Default) {
            val distinct = deletedMediaList.distinctBy { msg ->
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

                sourceFromFileName ?: sourceFromPath ?: "N|${msg.notificationKey}"
            }
            val grouped = distinct.groupBy { recoveredMessage ->
                SimpleDateFormat(
                    "MMMM d, yyyy",
                    Locale.getDefault()
                ).format(Date(recoveredMessage.timestamp))
            }
            grouped to distinct.map { it.id }
        }
        groupedMessages = grouped
        visibleAttachmentIds = visibleIds
        onVisibleAttachmentIds(visibleIds)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }

        }
        LaunchedEffect(pagerState.currentPage) {
            onCurrentTabChanged(pagerState.currentPage)
        }
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1
        ) { index ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = if (index == 0) 0.dp else 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 16.dp
                )
            ) {
                if (index == 0) {
                    // Notifications Tab
                    // Recovered text messages
                    if (conversationList.isEmpty()) {
                        item { EmptyStateScreen() }
                    } else {
                        items(
                            items = conversationList,
                            key = { msg -> msg.id }
                        ) { message ->
                            val displayName = MessageUtils.getConversationDisplayName(message)
                            ConversationItem(
                                message = message,
                                displayName = displayName,
                                isSelected = isConversationSelectionMode &&
                                        selectedConversationIds.contains(message.conversationId),
                                onClick = {
                                    if (isConversationSelectionMode) {
                                        onConversationClick(message.conversationId)
                                    } else {
                                        val conversationIdEncoded =
                                            Uri.encode(message.conversationId)
                                        val displayNameEncoded = Uri.encode(displayName)
                                        navController.navigate("detail/$conversationIdEncoded/$displayNameEncoded")
                                    }
                                },
                                onLongClick = { onConversationLongPress(message.conversationId) },
                            )
                        }
                    }
                } else {
                    // Attachments Tab
                    // Recovered media files
                    if (deletedMediaList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "📂", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.no_deleted_attachments_yet),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.deleted_media_will_show_here_once_recovered),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        groupedMessages.forEach { (date, messagesInGroup) ->
                            item { DisplayDateBadge(date) }
                            items(
                                items = messagesInGroup,
                                key = { msg -> msg.id }
                            ) { mediaMessage ->
                                Attachments(
                                    recoveredMessage = mediaMessage,
                                    isSelectionMode = isAttachmentSelectionMode,
                                    isSelected = selectedAttachmentIds.contains(mediaMessage.id),
                                    onClick = { onAttachmentClick(mediaMessage) },
                                    onLongClick = { onAttachmentLongPress(mediaMessage) }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}