package com.ziaafridi.notifyvault.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utility object containing commonly used icons for the app
 * Uses available Material Icons to avoid compilation errors
 */
object AppIcons {
    
    // Media Icons
    val Image = Icons.Default.Image
    val Video = Icons.Default.Movie // Alternative to VideoFile
    val Audio = Icons.Default.MusicNote // Alternative to AudioFile
    val Document = Icons.Default.Description
    val Attachment = Icons.Default.Attachment // Alternative to AttachFile
    val Mic = Icons.Default.Mic
    val VoiceNote = Icons.Default.Mic
    
    // Action Icons
    val Play = Icons.Default.PlayArrow
    val Delete = Icons.Default.Delete
    val DeleteForever = Icons.Default.DeleteForever // Alternative to DeleteSweep
    val Close = Icons.Default.Close
    val Check = Icons.Default.Check
    val CheckBox = Icons.Default.CheckBox // Alternative to Checklist
    val Clear = Icons.Default.Clear
    val Launch = Icons.Default.Launch // Alternative to OpenInNew
    
    // Navigation Icons
    val ArrowBack = Icons.Default.ArrowBack
    val MoreVert = Icons.Default.MoreVert
    val Settings = Icons.Default.Settings
    
    // Folder and File Icons
    val Folder = Icons.Default.FolderOpen // Alternative to Folder
    val Warning = Icons.Default.Warning
    
    // Conversation Icons
    val Person = Icons.Default.Person
    val Group = Icons.Default.Group
    val Chat = Icons.Default.Chat
    
    // Status Icons
    val Done = Icons.Default.Done
    val Error = Icons.Default.Error
    val Info = Icons.Default.Info
    
    /**
     * Gets appropriate icon for media type
     */
    fun getMediaIcon(mediaType: String?): ImageVector {
        return when (mediaType?.uppercase()) {
            "IMAGE" -> Image
            "VIDEO" -> Video
            "AUDIO" -> Audio
            "DOCUMENT" -> Document
            "STICKER" -> Image
            "GIF" -> Image
            else -> Attachment
        }
    }
    
    /**
     * Gets appropriate icon for conversation type
     */
    fun getConversationIcon(isGroup: Boolean): ImageVector {
        return if (isGroup) Group else Person
    }
    
    /**
     * Gets appropriate play icon for media type
     */
    fun getPlayIcon(mediaType: String?): ImageVector {
        return when (mediaType?.uppercase()) {
            "AUDIO" -> if (mediaType.contains("PTT", ignoreCase = true)) Mic else Audio
            "VIDEO" -> Play
            else -> Play
        }
    }
}

/**
 * Extension functions for common icon operations
 */

/**
 * Gets the first letter of a string for avatar display
 */
fun String.getAvatarLetter(): String {
    return this.trim().firstOrNull()?.uppercase() ?: "?"
}

/**
 * Determines if a filename indicates a voice message
 */
fun String?.isVoiceMessage(): Boolean {
    return this?.contains("PTT", ignoreCase = true) == true
}

/**
 * Gets appropriate content description for media icons
 */
fun getMediaContentDescription(mediaType: String?, fileName: String?): String {
    return when (mediaType?.uppercase()) {
        "IMAGE" -> "Image: ${fileName ?: "Unknown"}"
        "VIDEO" -> "Video: ${fileName ?: "Unknown"}"
        "AUDIO" -> if (fileName.isVoiceMessage()) "Voice message" else "Audio: ${fileName ?: "Unknown"}"
        "DOCUMENT" -> "Document: ${fileName ?: "Unknown"}"
        "STICKER" -> "Sticker"
        "GIF" -> "GIF: ${fileName ?: "Unknown"}"
        else -> "Media file: ${fileName ?: "Unknown"}"
    }
}