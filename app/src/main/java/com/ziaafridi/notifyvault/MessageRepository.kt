package com.ziaafridi.notifyvault

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MessageRepository(private val context: Context) {
    
    private val db = AppDatabase.getDatabase(context)
    private val messageDao = db.messageDao()
    
    /**
     * Deletes a single message and its associated media file
     */
    suspend fun deleteMessage(message: RecoveredMessage) = withContext(Dispatchers.IO) {
        // Delete media file if exists
        message.mediaPath?.let { mediaPath ->
            try {
                val file = File(mediaPath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Log error but continue with database deletion
            }
        }
        
        // Delete from database
        messageDao.deleteMessage(message.id)
    }
    
    /**
     * Deletes multiple messages and their associated media files
     */
    suspend fun deleteMessages(messages: List<RecoveredMessage>) = withContext(Dispatchers.IO) {
        // Delete media files
        messages.forEach { message ->
            message.mediaPath?.let { mediaPath ->
                try {
                    val file = File(mediaPath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue
                }
            }
        }
        
        // Delete from database
        val messageIds = messages.map { it.id }
        messageDao.deleteMessages(messageIds)
    }
    
    /**
     * Deletes an entire conversation and all its media files
     */
    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        // Get all messages in conversation to delete their media files
        val messages = messageDao.getAllMessagesForConversation(conversationId)
        
        // Delete media files
        messages.forEach { message ->
            message.mediaPath?.let { mediaPath ->
                try {
                    val file = File(mediaPath)
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    // Log error but continue
                }
            }
        }
        
        // Delete conversation from database
        messageDao.deleteConversation(conversationId)
    }
    
    /**
     * Gets message count for a conversation
     */
    suspend fun getMessageCount(conversationId: String): Int = withContext(Dispatchers.IO) {
        messageDao.getMessageCountInConversation(conversationId)
    }
}