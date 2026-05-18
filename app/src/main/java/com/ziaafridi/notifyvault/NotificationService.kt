package com.ziaafridi.notifyvault

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class NotificationService : NotificationListenerService() {

    private lateinit var db: AppDatabase
    private lateinit var mediaManager: MediaManager

    private val processedNotifications = ConcurrentHashMap<String, Long>()
    private var lastCleanup = 0L

    companion object {
        private const val TAG = "WAMR_Service"
//        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val DUPLICATE_TIME_WINDOW = 500L
        private const val CLEANUP_INTERVAL = 300000L // 5 minutes
//        private const val CLEANUP_RETENTION = 86400000L // 24 hours

        private val DELETION_KEYWORDS = listOf(
            "deleted",
            "delete",
            "removed",
            "this message was deleted",
            "message was deleted"
        )
        private val MESSAGE_KEYWORDS =
            listOf("message", "messages", "you", "media", "this", "deleting")
    }

    // Lifecycle

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
        mediaManager = MediaManager(this)
        Log.d(TAG, "NotificationService created")
    }

    // Core Callbacks

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        Log.d(TAG, "Notification received from: ${sbn?.packageName}")

        val notification = sbn ?: return

        if (!isValidWhatsAppNotification(notification)) return

        try {
            processIncomingNotification(notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }

        // Periodic cleanup
        performPeriodicCleanup()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        val notification = sbn ?: return

        if (!isValidWhatsAppNotification(notification)) return

        val extras = notification.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        if (title.equals("WhatsApp", ignoreCase = true)) return

        val content = extractMessageContent(extras)

        // Only trigger recovery if the removed notification looks like media
        if (!MessageUtils.isMediaMessage(content) && mediaManager.detectMediaType(content) == MediaManager.MediaType.UNKNOWN) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val extras = notification.notification.extras ?: return@launch

            // Detect if group using the system flag
            val isGroupFlag = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION)

            // handles replies in groups
            val convTitle =
                extras.getCharSequence(NotificationCompat.EXTRA_CONVERSATION_TITLE)?.toString()
                    ?.trim()
                    ?: title.trim()

            val info = determineConversationInfo(convTitle, content, isGroupFlag)

            // Determine sender for recovery logic
            val sender = if (info.type == ConversationType.GROUP) {
                MessageUtils.extractSenderFromGroupMessage(content) ?: title.trim()
            } else title.trim()

            val candidates = getRecentCandidates(info, content, notification.postTime, sender)

            supervisorScope {
                candidates.map { async { processMediaRecovery(it) } }.awaitAll()
            }
        }
    }

    // Processing Logic

    private fun processIncomingNotification(sbn: StatusBarNotification) {
        val notificationKey = generateNotificationKey(sbn)
        if (isAlreadyProcessed(notificationKey)) return

        val extras = sbn.notification.extras ?: return

        val rawTitle =
            extras.getCharSequence(NotificationCompat.EXTRA_CONVERSATION_TITLE)?.toString()
                ?: extras.getString(Notification.EXTRA_TITLE) ?: "Unknown"


        val sanitizedTitle = MessageUtils.sanitizeConversationName(rawTitle)

        if (rawTitle.equals("WhatsApp", ignoreCase = true)) return

        val messageContent = extractMessageContent(extras)

        var cleanContent = messageContent

        val timestamp = sbn.postTime
        val picture = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        val profileBitmap = extractProfileBitmap(sbn, extras)

        // Use system flags to detect groups
        val isGroupFlag = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION)
        val isGroup = isGroupFlag || MessageUtils.isGroupConversation(rawTitle, messageContent)

        if (isGroup) {
            // Remove group name prefix and message counts from group notifications.
            val groupNamePattern = Regex("^${Regex.escape(rawTitle)}.*?:\\s*", RegexOption.IGNORE_CASE)
            val sanitizedPattern = Regex("^${Regex.escape(sanitizedTitle)}.*?:\\s*", RegexOption.IGNORE_CASE)

            cleanContent = messageContent
                .replace(groupNamePattern, "")
                .replace(sanitizedPattern, "")
                .trim()

            // If it still starts with a colon after cleaning, remove it
            if (cleanContent.startsWith(":")) {
                cleanContent = cleanContent.substring(1).trim()
            }
        }

        val conversationInfo =
            determineConversationInfo(sanitizedTitle, messageContent, isGroupFlag)

        // generate id using sanitized title imp for thread grouping
        val conversationId = if (isGroup) {
            "group_${sanitizedTitle.lowercase().hashCode()}"
        } else {
            "individual_${sanitizedTitle.lowercase().hashCode()}"
        }

        // Check for Deletion Notifications
        if (isDeletionText(sanitizedTitle, rawTitle, messageContent)) {
            Log.d(TAG, "Deletion notification detected for ${conversationInfo.id}")
            handleDeletionEvent(conversationInfo, messageContent, timestamp, sanitizedTitle)
            markAsProcessed(notificationKey)
            return
        }

        // Determine the sender
        val actualSender = if (isGroup) {
            // In groups, the person's name is usually in EXTRA_TITLE
            MessageUtils.extractSenderFromGroupMessage(messageContent)
                ?: extras.getString(Notification.EXTRA_TITLE)?.trim()
                ?: sanitizedTitle
        } else {
            sanitizedTitle
        }

        val actualMessage = extractActualMessage(conversationInfo, messageContent)
        // Now extract the final message text without the sender's name
        val finalMessageBody = if (isGroup) {
            MessageUtils.extractContentFromGroupMessage(cleanContent)
        } else {
            cleanContent
        }

        val mediaPath = picture?.let { saveBitmapToFile(it, timestamp) }
        val profileImagePath =
            profileBitmap?.let { saveProfileBitmapToFile(it, conversationId, timestamp) }
        val messageHash =
            generateMessageHash(conversationInfo.id, actualMessage, timestamp, actualSender)
        val mediaType = mediaManager.detectMediaType(actualMessage)
        val mediaFileName = mediaManager.extractMediaFileName(actualMessage)
            ?: mediaManager.generateFallbackFileName(mediaType, timestamp)


        val message = RecoveredMessage(
            sender = sanitizedTitle,
            message = if (isGroup) "$actualSender: $finalMessageBody" else finalMessageBody,
            timestamp = timestamp,
            mediaPath = mediaPath,
            mediaType = if (mediaType != MediaManager.MediaType.UNKNOWN) mediaType.name else null,
            mediaFileName = mediaFileName,
            isDeleted = false,
            conversationId = conversationId,
            conversationType = if (isGroup) ConversationType.GROUP else ConversationType.INDIVIDUAL,
            notificationKey = notificationKey,
            messageHash = messageHash,
            profileImagePath = profileImagePath
        )

        Log.d(TAG, "Created message object - ID: ${message.id}, Sender: ${message.sender}, ConversationId: ${message.conversationId}")

        saveMessage(message)

        // Trigger Async Media Search (if media type detected but file not yet captured)
        if (mediaType != MediaManager.MediaType.UNKNOWN && mediaPath == null) {
            startAsyncMediaSearch(notificationKey, mediaType, mediaFileName, timestamp)
        }
        markAsProcessed(notificationKey)
    }

    // Media & Recovery

    private fun handleDeletionEvent(
        info: ConversationInfo,
        content: String,
        timestamp: Long,
        fallbackSender: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500) // Let the OS/File system settle

            val sender = if (info.type == ConversationType.GROUP) {
                MessageUtils.extractSenderFromGroupMessage(content) ?: fallbackSender
            } else fallbackSender

            val candidates = getRecentCandidates(info, content, timestamp, sender)
            if (candidates.isEmpty()) return@launch

            val toMark = candidates.filter { c ->
                val hasMediaHint =
                    !c.mediaType.isNullOrBlank() || MessageUtils.isMediaMessage(c.message)
                val missingFile = c.mediaPath.isNullOrBlank() || !File(c.mediaPath).exists()
                hasMediaHint || missingFile
            }

            supervisorScope {
                toMark.map { recoveredMessage -> async { processMediaRecovery(recoveredMessage) } }
                    .awaitAll()
            }
        }
        DeletedMessageRecoveryScheduler.schedule(this)
    }

    private suspend fun processMediaRecovery(candidate: RecoveredMessage) {
        // Mark as deleted immediately to update the UI
        val currentMessage = candidate.copy(isDeleted = true)
        db.messageDao().update(currentMessage)

        // Only proceed if the media file is missing
        val needsMedia = currentMessage.mediaPath.isNullOrBlank() || !File(currentMessage.mediaPath).exists()
        if (!needsMedia) return

        // Poll for media as it may take time to appear in the cache
        val maxAttempts = 15
        val pollIntervalMs = 2000L // 2 secs

        for (attempt in 1..maxAttempts) {
            delay(pollIntervalMs) // Wait for WA to finish downloading

            val recovered = mediaManager.findAndCopyMediaByType(
                mediaType = currentMessage.mediaType?.let { MediaManager.MediaType.valueOf(it) } ?: MediaManager.MediaType.UNKNOWN,
                fileNameHint = currentMessage.mediaFileName,
                timestamp = currentMessage.timestamp
            )

            if (recovered.isAvailable && !recovered.localPath.isNullOrBlank()) {
                db.messageDao().update(currentMessage.copy(
                    mediaPath = recovered.localPath,
                    mediaFileName = recovered.fileName
                ))
                Log.d(TAG, "Success: Media recovered on attempt $attempt")
                return
            }
        }
    }


    private fun startAsyncMediaSearch(
        notificationKey: String,
        mediaType: MediaManager.MediaType,
        fileName: String,
        timestamp: Long
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val maxAttempts = 8
                val pollIntervalMs = 500L // Check every half second

                for (attempt in 1..maxAttempts) {
                    val mediaInfo =
                        mediaManager.findAndCopyMediaByType(mediaType, fileName, timestamp)

                    if (mediaInfo.isAvailable && !mediaInfo.localPath.isNullOrBlank()) {
                        db.messageDao().updateMediaByNotificationKey(
                            notificationKey = notificationKey,
                            mediaPath = mediaInfo.localPath,
                            mediaType = mediaType.name,
                            mediaFileName = mediaInfo.fileName
                        )
                        Log.d(TAG, "Media recovered fast on attempt $attempt")
                        break // Success! Exit the loop.
                    }

                    // If not found and not the last attempt, wait briefly before trying again
                    if (attempt < maxAttempts) {
                        delay(pollIntervalMs)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for media", e)
            }
        }
    }

    private suspend fun getRecentCandidates(
        info: ConversationInfo,
        content: String,
        time: Long,
        specificSender: String? = null
    ): List<RecoveredMessage> {
        val window = 30 * 60 * 1000L
        val senderFallback = specificSender ?: (if (info.type == ConversationType.GROUP) {
            MessageUtils.extractSenderFromGroupMessage(content) ?: info.displayName
        } else info.displayName)

        val senderMessages = db.messageDao()
            .getUndeletedMessagesForSenderInRange(senderFallback, time - window, time + window, 10)
        val chatMessages = db.messageDao()
            .getUndeletedMessagesForConversationInRange(info.id, time - window, time + window, 10)

        val all =
            if (info.type == ConversationType.INDIVIDUAL) senderMessages else chatMessages.ifEmpty { senderMessages }
        return all.take(5)
    }

    // Utility / Parsing

    private fun isValidWhatsAppNotification(sbn: StatusBarNotification): Boolean {
        val isWhatsApp = sbn.packageName?.contains("whatsapp", ignoreCase = true) == true
        val isSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
        return isWhatsApp && !isSummary
    }

    private fun isDeletionText(convTitle: String, title: String, content: String): Boolean {
        val combinedLower = "$convTitle $title $content".lowercase()
        val hasKeyword = DELETION_KEYWORDS.any { combinedLower.contains(it) }
        val hasContext = MESSAGE_KEYWORDS.any { combinedLower.contains(it) }
        return hasKeyword && hasContext
    }

//    private fun determineActualSender(
//        info: ConversationInfo,
//        content: String,
//        fallback: String
//    ): String {
//        return if (info.type == ConversationType.GROUP) {
//            MessageUtils.extractSenderFromGroupMessage(content) ?: fallback
//        } else fallback
//    }

    private fun extractActualMessage(info: ConversationInfo, content: String): String {
        return if (info.type == ConversationType.GROUP) {
            MessageUtils.extractContentFromGroupMessage(content)
        } else content
    }

    private fun extractMessageContent(extras: Bundle): String {
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)

        if (!lines.isNullOrEmpty()) {
            val trimmedLines =
                lines.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
            val mediaLine = trimmedLines.asReversed().firstOrNull { line ->
                mediaManager.detectMediaType(line) != MediaManager.MediaType.UNKNOWN ||
                        !mediaManager.extractMediaFileName(line).isNullOrBlank()
            }
            if (!mediaLine.isNullOrBlank()) return mediaLine

            val lastNonEmpty = trimmedLines.lastOrNull()
            if (!lastNonEmpty.isNullOrBlank()) return lastNonEmpty
        }
        return bigText.ifBlank { text }
    }

    // if missing or update existing
    private fun determineConversationInfo(
        title: String,
        message: String,
        isGroupHint: Boolean
    ): ConversationInfo {
        val isGroup = isGroupHint || MessageUtils.isGroupConversation(title, message)
        val sanitizedTitle = MessageUtils.sanitizeConversationName(title)

        return if (isGroup) {
            ConversationInfo(
                id = "group_${sanitizedTitle.lowercase().hashCode()}",
                type = ConversationType.GROUP,
                displayName = sanitizedTitle
            )
        } else {
            ConversationInfo(
                id = "individual_${sanitizedTitle.lowercase().hashCode()}",
                type = ConversationType.INDIVIDUAL,
                displayName = sanitizedTitle
            )
        }
    }

//    private fun cleanGroupName(groupTitle: String): String {
//        return groupTitle.trim()
//            .replace(Regex("\\s*\\(\\d+\\)\\s*"), "")
//            .replace(Regex("\\s*\\(\\d+\\s+participants?\\)\\s*"), "")
//            .replace(Regex("\\s*\\(\\d+\\s+members?\\)\\s*"), "")
//            .replace(Regex("\\s*-\\s*WhatsApp\\s*$"), "")
//            .trim()
//    }

    private fun generateNotificationKey(sbn: StatusBarNotification): String {
        val title = sbn.notification.extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text =
            sbn.notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val contentHash = (title + text).hashCode().toString(16)
        return "${sbn.packageName}_${sbn.id}_${sbn.postTime}_${contentHash}"
    }

    private fun generateMessageHash(
        convId: String,
        msg: String,
        timestamp: Long,
        sender: String
    ): String {
        val input = "$convId:$sender:$msg:${timestamp / 1000}"
        return MessageDigest.getInstance("MD5").digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun isAlreadyProcessed(key: String): Boolean {
        val lastProcessed = processedNotifications[key] ?: return false
        return (System.currentTimeMillis() - lastProcessed) < DUPLICATE_TIME_WINDOW
    }

    private fun markAsProcessed(key: String) {
        processedNotifications[key] = System.currentTimeMillis()
    }

    private fun saveMessage(message: RecoveredMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existsByExact = db.messageDao().existsByExactContent(
                    message.conversationId,
                    message.sender,
                    message.message,
                    message.timestamp,
                    30_000L
                )
                val existsByHash = db.messageDao().existsByContentHash(
                    message.messageHash, message.conversationId, message.timestamp, 30_000L
                )

                if (existsByExact == 0 && existsByHash == 0) {
                    db.messageDao().insert(message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving message", e)
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, timestamp: Long): String? {
        return try {
            val file = File(filesDir, "media_$timestamp.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    out
                )
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun extractProfileBitmap(sbn: StatusBarNotification, extras: Bundle): Bitmap? {
        // WhatsApp typically sets the sender avatar as the notification largeIcon.
        // try extras bitmaps first, then the Icon API.
        runCatching {
            val fromCompat = extras.getParcelable<Bitmap>(NotificationCompat.EXTRA_LARGE_ICON)
            if (fromCompat != null) return fromCompat
        }

        runCatching {
            val fromBig = extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON_BIG)
            if (fromBig != null) return fromBig
        }

        runCatching {
            @Suppress("DEPRECATION")
            val fromLegacy = extras.getParcelable<Bitmap>(Notification.EXTRA_LARGE_ICON)
            if (fromLegacy != null) return fromLegacy
        }

        return runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val icon: Icon? = sbn.notification.getLargeIcon()
                icon?.loadDrawable(this)?.toBitmap()
            } else null
        }.getOrNull()
    }

    private fun saveProfileBitmapToFile(
        bitmap: Bitmap,
        conversationId: String,
        timestamp: Long
    ): String? {
        return runCatching {
            val dir = File(filesDir, "avatars").apply { mkdirs() }
            val safeId = conversationId.hashCode().toString(16)
            val file = File(dir, "avatar_${safeId}_$timestamp.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        }.getOrNull()
    }

    private fun performPeriodicCleanup() {
        val now = System.currentTimeMillis()
        if (now - lastCleanup > CLEANUP_INTERVAL) {
            lastCleanup = now

            // Clean up in-memory processed notifications cache to prevent map from growing indefinitely.
            val cutoff = now - (DUPLICATE_TIME_WINDOW * 2)
            processedNotifications.entries.removeIf { it.value < cutoff }

            // Clean up old messages from database
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    val dbCutoff = now - CLEANUP_RETENTION
//                    db.messageDao().cleanupOldMessages(dbCutoff)
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error during database cleanup", e)
//                }
//            }
        }
    }


    private data class ConversationInfo(
        val id: String,
        val type: ConversationType,
        val displayName: String
    )
}