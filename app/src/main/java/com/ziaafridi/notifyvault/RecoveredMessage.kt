package com.ziaafridi.notifyvault

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "recovered_messages",
    indices = [
        Index(value = ["notificationKey"], unique = true),
        Index(value = ["conversationId", "timestamp"]),
        Index(value = ["sender", "timestamp"])
    ]
)
data class RecoveredMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val mediaPath: String? = null,
    val mediaType: String? = null, // MediaType enum as string
    val mediaFileName: String? = null,
    val isDeleted: Boolean = false,
    val conversationId: String, // Unique identifier for conversation (individual or group)
    val conversationType: ConversationType,
    val notificationKey: String, // Unique key to prevent duplicate processing
    val messageHash: String, // Hash of content for additional duplicate detection
    val profileImagePath: String? = null, // Saved contact avatar from notification (if available)
)

enum class ConversationType {
    INDIVIDUAL,
    GROUP
}