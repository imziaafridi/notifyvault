package com.ziaafridi.notifyvault

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // Save a new message, ignoring duplicates
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: RecoveredMessage): Long

    // Save a new message, throwing an error on duplicates
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWithResult(message: RecoveredMessage): Long

    // Get the most recent message from every conversation
    @Query("SELECT * FROM recovered_messages WHERE id IN (SELECT MAX(id) FROM recovered_messages GROUP BY conversationId) ORDER BY timestamp DESC")
    fun getLatestConversations(): Flow<List<RecoveredMessage>>

    // Observe all messages in a conversation as a stream
    @Query("SELECT * FROM recovered_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<RecoveredMessage>>

    // Fetch messages in pages for a conversation (newest first)
    @Query(
        "SELECT * FROM recovered_messages " +
            "WHERE conversationId = :conversationId " +
            "ORDER BY timestamp DESC " +
            "LIMIT :limit OFFSET :offset"
    )
    suspend fun getMessagesForConversationPaged(
        conversationId: String,
        limit: Int,
        offset: Int
    ): List<RecoveredMessage>

    // Fetch all messages in a conversation once
    @Query("SELECT * FROM recovered_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getAllMessagesForConversation(conversationId: String): List<RecoveredMessage>

    // Observe deleted messages filtered by sender
    @Query("SELECT * FROM recovered_messages WHERE sender = :sender ORDER BY timestamp ASC")
    fun getDeletedMessagesForSender(sender: String): Flow<List<RecoveredMessage>>

    // Observe all deleted messages within a specific conversation
    @Query("SELECT * FROM recovered_messages WHERE conversationId = :conversationId AND isDeleted = 1 ORDER BY timestamp ASC")
    fun getDeletedMessagesForConversation(conversationId: String): Flow<List<RecoveredMessage>>

    // Find the last 10 messages in a conversation within a time range
    @Query("SELECT * FROM recovered_messages WHERE conversationId = :conversationId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC LIMIT 10")
    suspend fun findRecentMessagesByConversation(
        conversationId: String,
        startTime: Long,
        endTime: Long
    ): List<RecoveredMessage>

    // Check if a message exists using its notification key
    @Query("SELECT COUNT(*) FROM recovered_messages WHERE notificationKey = :notificationKey")
    suspend fun existsByNotificationKey(notificationKey: String): Int

    // Check if a similar message exists based on content hash and time
    @Query("SELECT COUNT(*) FROM recovered_messages WHERE messageHash = :messageHash AND conversationId = :conversationId AND ABS(timestamp - :timestamp) < :timeWindow")
    suspend fun existsByContentHash(
        messageHash: String,
        conversationId: String,
        timestamp: Long,
        timeWindow: Long = 2000
    ): Int

    // Check if a message exists based on exact text and time
    @Query("SELECT COUNT(*) FROM recovered_messages WHERE conversationId = :conversationId AND sender = :sender AND message = :message AND ABS(timestamp - :timestamp) < :timeWindow")
    suspend fun existsByExactContent(
        conversationId: String,
        sender: String,
        message: String,
        timestamp: Long,
        timeWindow: Long = 2000
    ): Int

    // Update the local file path for a message by its hash
    @Query("UPDATE recovered_messages SET mediaPath = :mediaPath WHERE messageHash = :messageHash")
    suspend fun updateMediaPath(messageHash: String, mediaPath: String)

    // Update media details for a message identified by its content hash
    @Query(
        "UPDATE recovered_messages SET mediaPath = :mediaPath, mediaType = :mediaType, mediaFileName = :mediaFileName " +
                "WHERE messageHash = :messageHash"
    )
    suspend fun updateMediaByHash(
        messageHash: String,
        mediaPath: String,
        mediaType: String?,
        mediaFileName: String?
    )

    // Update media details for a message using its unique notification key
    @Query(
        "UPDATE recovered_messages SET mediaPath = :mediaPath, mediaType = :mediaType, mediaFileName = :mediaFileName " +
                "WHERE notificationKey = :notificationKey"
    )
    suspend fun updateMediaByNotificationKey(
        notificationKey: String,
        mediaPath: String,
        mediaType: String?,
        mediaFileName: String?
    )

    // Delete a specific message by ID
    @Query("DELETE FROM recovered_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Int)

    // Delete multiple messages by their IDs
    @Query("DELETE FROM recovered_messages WHERE id IN (:messageIds)")
    suspend fun deleteMessages(messageIds: List<Int>)

    // Delete an entire conversation
    @Query("DELETE FROM recovered_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    // Count how many messages are in a conversation
    @Query("SELECT COUNT(*) FROM recovered_messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountInConversation(conversationId: String): Int

    // Permanently remove messages older than a specific date
    @Query("DELETE FROM recovered_messages WHERE timestamp < :cutoffTime")
    suspend fun cleanupOldMessages(cutoffTime: Long)

    // Update an existing message record
    @Update
    suspend fun update(message: RecoveredMessage)

    // Get the very last message received in a conversation
    @Query("SELECT * FROM recovered_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageByConversationId(conversationId: String): RecoveredMessage?

    // Get the last message sent by a specific contact
    @Query("SELECT * FROM recovered_messages WHERE sender = :sender ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageBySender(sender: String): RecoveredMessage?

    // Find the closest non-deleted message to a given timestamp in a conversation
    @Query(
        "SELECT * FROM recovered_messages " +
                "WHERE conversationId = :conversationId AND isDeleted = 0 " +
                "AND ABS(timestamp - :timestamp) < :timeWindow " +
                "ORDER BY ABS(timestamp - :timestamp) ASC LIMIT 1"
    )
    suspend fun getClosestUndeletedMessageByConversationId(
        conversationId: String,
        timestamp: Long,
        timeWindow: Long
    ): RecoveredMessage?

    // Get a list of active messages within a time range for a conversation
    @Query(
        "SELECT * FROM recovered_messages " +
                "WHERE conversationId = :conversationId AND isDeleted = 0 " +
                "AND timestamp BETWEEN :startTime AND :endTime " +
                "ORDER BY timestamp DESC LIMIT :limit"
    )
    suspend fun getUndeletedMessagesForConversationInRange(
        conversationId: String,
        startTime: Long,
        endTime: Long,
        limit: Int
    ): List<RecoveredMessage>

    // Find the closest non-deleted message from a specific sender
    @Query(
        "SELECT * FROM recovered_messages " +
                "WHERE sender = :sender AND isDeleted = 0 " +
                "AND ABS(timestamp - :timestamp) < :timeWindow " +
                "ORDER BY ABS(timestamp - :timestamp) ASC LIMIT 1"
    )
    suspend fun getClosestUndeletedMessageBySender(
        sender: String,
        timestamp: Long,
        timeWindow: Long
    ): RecoveredMessage?

    // Get a list of active messages from a sender within a time range
    @Query(
        "SELECT * FROM recovered_messages " +
                "WHERE sender = :sender AND isDeleted = 0 " +
                "AND timestamp BETWEEN :startTime AND :endTime " +
                "ORDER BY timestamp DESC LIMIT :limit"
    )
    suspend fun getUndeletedMessagesForSenderInRange(
        sender: String,
        startTime: Long,
        endTime: Long,
        limit: Int
    ): List<RecoveredMessage>

    // Mark the most recent active message in a conversation as deleted
    @Query("UPDATE recovered_messages SET isDeleted = 1 WHERE id = (SELECT id FROM recovered_messages WHERE conversationId = :conversationId AND isDeleted = 0 ORDER BY timestamp DESC LIMIT 1)")
    suspend fun markLatestMessageAsDeleted(conversationId: String): Int

    // Get all deleted messages that are missing their media files
    @Query("SELECT * FROM recovered_messages WHERE isDeleted = 1 AND (mediaPath IS NULL OR mediaPath = '') ORDER BY timestamp DESC")
    suspend fun getDeletedMessagesWithoutMedia(): List<RecoveredMessage>

    // Observe all messages that contain media
    @Query("SELECT * FROM recovered_messages WHERE mediaPath IS NOT NULL ORDER BY timestamp DESC")
    fun getAllMediaMessages(): Flow<List<RecoveredMessage>>

    // Update the media path and filename for a specific message ID
    @Query("UPDATE recovered_messages SET mediaPath = :mediaPath, mediaFileName = :mediaFileName WHERE id = :messageId")
    suspend fun updateMediaById(messageId: Long, mediaPath: String, mediaFileName: String?)
}
