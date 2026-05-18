package com.ziaafridi.notifyvault

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConversationItem(
    message: RecoveredMessage,
    displayName: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val previewText = remember(message.message) {
        val memberName = MessageUtils.extractSenderFromGroupMessage(message.message)
        if (memberName != null) MessageUtils.extractContentFromGroupMessage(message.message) else message.message
    }

    val timeText = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val avatarBitmap by remember(message.profileImagePath) {
        mutableStateOf(
            message.profileImagePath
                ?.takeIf { it.isNotBlank() }
                ?.let { path -> runCatching { BitmapFactory.decodeFile(path) }.getOrNull() }
                ?.takeIf { bmp -> bmp.width > 0 && bmp.height > 0 }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                else Color.Transparent
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = displayName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

//        Divider(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 80.dp),
//            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
//        )
    }
}