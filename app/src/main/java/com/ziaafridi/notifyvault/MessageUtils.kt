package com.ziaafridi.notifyvault

import java.text.SimpleDateFormat
import java.util.*

object MessageUtils {

    /**
     * Formats a timestamp into a readable date and time string.
     */
    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Extracts the sender's name from a group message format (e.g., "John: Hello").
     */
    fun extractSenderFromGroupMessage(message: String): String? {
        val colonIndex = message.indexOf(":")
        return if (colonIndex > 0 && colonIndex < message.length / 2) {
            val sender = message.substring(0, colonIndex).trim()
            if (sender.isNotBlank()) sender else null
        } else null
    }

    /**
     * Extracts the actual text content from a group message, removing the sender prefix.
     */
    fun extractContentFromGroupMessage(message: String): String {
        val colonIndex = message.indexOf(":")
        return if (colonIndex > 0 && colonIndex < message.length / 2) {
            message.substring(colonIndex + 1).trim()
        } else message
    }

    /**
     * Determines if a notification title or message indicates a group conversation.
     */
    fun isGroupConversation(title: String, message: String): Boolean {
        return when {
            // Check for WhatsApp's "(X messages)" group indicator in the title
            title.contains(Regex("\\(\\d+.*messages?\\)")) -> true
            // Check for group delimiter in the title
            title.contains(":") -> true
            // Check if the message uses "Sender: Message" format (ignoring links)
            message.contains(":") && !message.startsWith("http") -> {
                val colonIndex = message.indexOf(":")
                colonIndex > 0 && colonIndex < message.length / 2
            }
            else -> false
        }
    }

    /**
     * Removes message counts and trailing colons from a conversation name.
     */
    fun sanitizeConversationName(name: String): String {
        return name.trim()
            .replace(Regex("\\s*\\([^)]*messages?\\)"), "")
            .replace(Regex("\\s*\\(\\d+\\)"), "")
            .replace(Regex(":\\s*$"), "")
            .trim()
    }

    /**
     * Identifies if the message text contains media placeholders (e.g., "📷 Photo").
     */
    fun isMediaMessage(message: String): Boolean {
        val mediaIndicators = listOf(
            "📷 Photo", "🎥 Video", "🎵 Audio", "📄 Document",
            "photo", "video", "audio", "document", "sticker",
            "📍 Location", "location shared"
        )
        return mediaIndicators.any { indicator ->
            message.contains(indicator, ignoreCase = true)
        }
    }

    /**
     * Limits message length for UI previews.
     */
    fun truncateMessage(message: String, maxLength: Int = 100): String {
        return if (message.length <= maxLength) message else "${message.take(maxLength)}..."
    }

    /**
     * Returns a clean display name for a conversation (Contact or Group name).
     */
    fun getConversationDisplayName(message: RecoveredMessage): String {
        return sanitizeConversationName(message.sender)
    }

    /**
     * Generates a preview string including sender name, media icons, and text.
     */
    fun getMessagePreview(message: RecoveredMessage): String {
        val isGroup = message.conversationType == ConversationType.GROUP

        val detailPart = when {
            message.mediaType != null || !message.mediaFileName.isNullOrBlank() -> {
                val icon = when (message.mediaType) {
                    "IMAGE" -> "📷 Photo"
                    "VIDEO" -> "🎥 Video"
                    "AUDIO" -> if (message.mediaFileName?.contains("PTT") == true) "🎤 Voice message" else "🎵 Audio"
                    "DOCUMENT" -> "📄 ${message.mediaFileName ?: "Document"}"
                    else -> "📄 Document"
                }

                val textContent = if (isGroup) extractContentFromGroupMessage(message.message) else message.message
                if (textContent.isNotBlank() && !isMediaMessage(textContent)) {
                    "$icon: $textContent"
                } else {
                    icon
                }
            }
            isGroup -> extractContentFromGroupMessage(message.message)
            else -> message.message
        }

        return if (isGroup) {
            val memberName = extractSenderFromGroupMessage(message.message)
            if (memberName != null) {
                "$memberName: ${truncateMessage(detailPart, 80)}"
            } else {
                truncateMessage(detailPart, 100)
            }
        } else {
            truncateMessage(detailPart, 100)
        }
    }
}