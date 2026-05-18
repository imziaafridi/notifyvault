package com.ziaafridi.notifyvault

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ziaafridi.notifyvault.ui.MediaViewer
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun Attachments(
    recoveredMessage: RecoveredMessage,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {

    if (!recoveredMessage.isDeleted) return

    if (!recoveredMessage.mediaPath.isNullOrEmpty() || recoveredMessage.mediaType != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .background(
                        if (isSelectionMode && isSelected)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .padding(12.dp)
            ) {
                val isDocument =
                    recoveredMessage.mediaType?.contains("document", ignoreCase = true) == true
                if (!recoveredMessage.mediaFileName.isNullOrBlank() && !isDocument) {
                    Text(
                        text = recoveredMessage.mediaFileName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Show real media only if we have a recovered file path.
                if (!recoveredMessage.mediaPath.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        MediaViewer(
                            message = recoveredMessage,
                            disableInteractions = isSelectionMode,
                        )
                        // Capture taps over the whole media region during selection (some inner types
                        // still consume gestures without this overlay).
                        if (isSelectionMode) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .combinedClickable(
                                        onClick = onClick,
                                        onLongClick = onLongClick
                                    )
                            )
                        }
                    }
                } else {
                    // placeholder card.
                    Text(
                        text = stringResource(R.string.media_recovered_after_deletion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // pushes time to right side
                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                            .format(Date(recoveredMessage.timestamp))
                            .lowercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}