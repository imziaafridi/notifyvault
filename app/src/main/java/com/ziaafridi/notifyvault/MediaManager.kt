package com.ziaafridi.notifyvault

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class MediaManager(private val context: Context) {

    private val mediaFolderManager = MediaFolderManager(context)

    companion object {
        private const val TAG = "MediaManager"

        // Media type patterns
        private val MEDIA_PATTERNS = mapOf(
            MediaType.IMAGE to listOf(
                "📷",
                "Photo",
                "image",
                "IMG-",
                "VID-",
                ".jpg",
                ".jpeg",
                ".png",
                ".gif",
                ".webp"
            ),
            MediaType.VIDEO to listOf(
                "🎥",
                "Video",
                "video",
                "VID-",
                ".mp4",
                ".3gp"
            ),
            MediaType.AUDIO to listOf(
                "🎵",
                "Audio",
                "🎤",
                "Voice",
                "voice note",
                "PTT-",
                ".opus",
                ".aac",
                ".m4a"
            ),
            MediaType.DOCUMENT to listOf(
                "📄",
                "Document",
                "DOC-",
                ".pdf",
                ".doc",
                ".docx",
                ".txt",
                ".xls",
                ".xlsx",
                ".ppt",
                ".pptx"
            ),
            MediaType.STICKER to listOf("Sticker", "sticker", ".webp"),
            MediaType.GIF to listOf("GIF", ".gif"),
            MediaType.LOCATION to listOf("📍", "Location", "location shared")
        )

        // WhatsApp media folder names
        private val WHATSAPP_MEDIA_FOLDERS = listOf(
            "WhatsApp Images",
            "WhatsApp Video",
            "WhatsApp Audio",
            "WhatsApp Documents",
            "WhatsApp Stickers",
            "WhatsApp Animated Gifs",
            "WhatsApp Voice Notes"
        )

        // File extensions by type
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        private val VIDEO_EXTENSIONS = setOf("mp4", "avi", "mov", "3gp", "mkv", "webm")
        private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "aac", "ogg", "m4a", "opus", "amr")
        private val DOCUMENT_EXTENSIONS =
            setOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")
    }

    enum class MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT, STICKER, GIF, LOCATION, UNKNOWN
    }

    data class MediaInfo(
        val type: MediaType,
        val fileName: String?,
        val localPath: String?,
        val isAvailable: Boolean
    )

    private data class CopiedMedia(
        val localPath: String,
        val sourceFileName: String
    )

    /**
     * Detects media type from message content
     */
    fun detectMediaType(message: String): MediaType {
        val lowerMessage = message.lowercase()

        for ((type, patterns) in MEDIA_PATTERNS) {
            if (patterns.any { pattern -> lowerMessage.contains(pattern.lowercase()) }) {
                return type
            }
        }

        return MediaType.UNKNOWN
    }

    /**
     * Extracts potential media filename from message
     */
    fun extractMediaFileName(message: String): String? {
        // Pattern for WhatsApp media filenames
        val patterns = listOf(
            Pattern.compile(
                "IMG-\\d{8}-WA\\d{4}\\.(jpg|jpeg|png|gif|webp)",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile("VID-\\d{8}-WA\\d{4}\\.(mp4|3gp)", Pattern.CASE_INSENSITIVE),
            Pattern.compile(
                "(AUD|PTT)-\\d{8}-WA\\d{4}\\.(opus|aac|m4a|mp3)",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile(
                "DOC-\\d{8}-WA\\d{4}\\.(pdf|doc|docx|txt|xls|xlsx)",
                Pattern.CASE_INSENSITIVE
            ),
            Pattern.compile("STK-\\d{8}-WA\\d{4}\\.webp", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                return matcher.group()
            }
        }

        // Try to extract filename from message content
        val words = message.split(" ")
        for (word in words) {
            if (word.contains(".") && isValidMediaExtension(word)) {
                return word
            }
        }

        return null
    }

    /**
     * Searches for media file in WhatsApp folders and copies to app folder
     */
    suspend fun findAndCopyMedia(message: String, timestamp: Long): MediaInfo =
        withContext(Dispatchers.IO) {
            val mediaType = detectMediaType(message)
            val extractedFileName = extractMediaFileName(message)

            if (mediaType == MediaType.UNKNOWN) {
                return@withContext MediaInfo(mediaType, extractedFileName, null, false)
            }

            // If WhatsApp didn't include the real filename in the notification text,
            // fall back to a synthetic filename so we can still search by timestamp + extension.
            val effectiveFileName =
                extractedFileName ?: generateFallbackFileName(mediaType, timestamp)

            Log.d(
                TAG,
                "Searching for media file. extractedFileName=$extractedFileName, effectiveFileName=$effectiveFileName, type=$mediaType"
            )

            // Check if file already exists in app folder
            val existingFile = File(getAppMediaFolder(), effectiveFileName)
            if (existingFile.exists()) {
                Log.d(TAG, "Media file already exists: ${existingFile.absolutePath}")
                return@withContext MediaInfo(
                    mediaType,
                    effectiveFileName,
                    existingFile.absolutePath,
                    true
                )
            }

            // Search in WhatsApp folders
            val copied = searchAndCopyFromWhatsApp(effectiveFileName, mediaType, timestamp)

            return@withContext MediaInfo(
                type = mediaType,
                fileName = copied?.sourceFileName ?: effectiveFileName,
                localPath = copied?.localPath,
                isAvailable = copied != null
            )
        }

    /**
     * Recovers/copies a WhatsApp media file using a known media type.
     * Useful for cases like "deleted message" notifications where the notification no longer contains the original filename.
     */
    suspend fun findAndCopyMediaByType(
        mediaType: MediaType,
        fileNameHint: String?,
        timestamp: Long
    ): MediaInfo = withContext(Dispatchers.IO) {
        if (mediaType == MediaType.UNKNOWN) {
            return@withContext MediaInfo(MediaType.UNKNOWN, fileNameHint, null, false)
        }

        val cleanedHint = fileNameHint?.takeIf { it.isNotBlank() }

        // If we don't have a real filename from the notification, try multiple likely extensions.
        // WhatsApp stores voice notes as .opus in many cases, not always .aac.
        val candidateExtensions: List<String> = when {
            cleanedHint != null -> listOf(getFileExtension(cleanedHint))
            mediaType == MediaType.IMAGE || mediaType == MediaType.GIF -> listOf(
                "jpg",
                "jpeg",
                "png",
                "gif",
                "webp",
                "bmp"
            )

            mediaType == MediaType.VIDEO -> listOf("mp4", "3gp", "mkv", "avi", "mov", "webm")
            mediaType == MediaType.AUDIO -> listOf("opus", "aac", "m4a", "mp3", "wav", "amr", "ogg")
            mediaType == MediaType.DOCUMENT -> listOf(
                "pdf",
                "doc",
                "docx",
                "txt",
                "xls",
                "xlsx",
                "ppt",
                "pptx"
            )

            mediaType == MediaType.STICKER -> listOf("webp")
            else -> listOf("bin")
        }.filter { it.isNotBlank() }

        val prefixSingle = when (mediaType) {
            MediaType.IMAGE -> "IMG"
            MediaType.VIDEO -> "VID"
            MediaType.AUDIO -> "AUD"
            MediaType.DOCUMENT -> "DOC"
            MediaType.STICKER -> "STK"
            MediaType.GIF -> "GIF"
            else -> "MEDIA"
        }

        // Voice notes in WhatsApp are commonly saved with PTT- prefix (not AUD-).
        // If we don't have a real filename hint, we try both AUD and PTT to recover voice notes.
        val prefixes: List<String> = when (mediaType) {
            MediaType.AUDIO -> {
                if (cleanedHint?.startsWith("PTT-") == true) {
                    listOf("PTT")
                } else {
                    listOf("AUD", "PTT")
                }
            }

            else -> listOf(prefixSingle)
        }

        // Try exact hint first (if it exists)
        if (cleanedHint != null) {
            val existingFile = File(getAppMediaFolder(), cleanedHint)
            if (existingFile.exists()) {
                return@withContext MediaInfo(
                    type = mediaType,
                    fileName = cleanedHint,
                    localPath = existingFile.absolutePath,
                    isAvailable = true
                )
            }
            val copied = searchAndCopyFromWhatsApp(cleanedHint, mediaType, timestamp)
            return@withContext MediaInfo(
                type = mediaType,
                fileName = copied?.sourceFileName ?: cleanedHint,
                localPath = copied?.localPath,
                isAvailable = copied != null
            )
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestamp))

        for (prefix in prefixes) {
            for (ext in candidateExtensions) {
                val effectiveFileName = "${prefix}-${dateStr}.$ext"

                val existingFile = File(getAppMediaFolder(), effectiveFileName)
                if (existingFile.exists()) {
                    return@withContext MediaInfo(
                        type = mediaType,
                        fileName = effectiveFileName,
                        localPath = existingFile.absolutePath,
                        isAvailable = true
                    )
                }

                Log.d(
                    TAG,
                    "findAndCopyMediaByType trying effectiveFileName=$effectiveFileName for mediaType=$mediaType"
                )
                val copied = searchAndCopyFromWhatsApp(effectiveFileName, mediaType, timestamp)
                if (copied != null) {
                    return@withContext MediaInfo(
                        type = mediaType,
                        fileName = copied.sourceFileName,
                        localPath = copied.localPath,
                        isAvailable = true
                    )
                }
            }
        }

        // Nothing matched any candidate extension
        return@withContext MediaInfo(
            type = mediaType,
            fileName = null,
            localPath = null,
            isAvailable = false
        )
    }

    /**
     * Searches WhatsApp media folders for the file
     */
    private suspend fun searchAndCopyFromWhatsApp(
        fileName: String,
        mediaType: MediaType,
        timestamp: Long
    ): CopiedMedia? {
        if (!mediaFolderManager.hasWhatsAppFolderAccess()) {
            Log.w(TAG, "No WhatsApp folder access granted")
            return null
        }

        val whatsappUri = mediaFolderManager.getWhatsAppFolderUri() ?: return null
        val whatsappFolder = DocumentFile.fromTreeUri(context, whatsappUri) ?: return null

        // Determine which subfolders to search based on type
        val foldersToSearch = when (mediaType) {
            MediaType.IMAGE, MediaType.GIF -> listOf("WhatsApp Images", "WhatsApp Animated Gifs")
            MediaType.VIDEO -> listOf("WhatsApp Video")
            MediaType.AUDIO -> listOf("WhatsApp Audio", "WhatsApp Voice Notes")
            MediaType.DOCUMENT -> listOf("WhatsApp Documents")
            MediaType.STICKER -> listOf("WhatsApp Stickers")
            else -> WHATSAPP_MEDIA_FOLDERS
        }

        for (folderName in foldersToSearch) {
            val mediaFolder = findFolderRecursively(whatsappFolder, folderName, 5)

            if (mediaFolder?.exists() == true) {
                var freshFile: DocumentFile? = null

                // NEW LOGIC FOR VOICE NOTES:
                if (folderName == "WhatsApp Voice Notes") {
                    // Voice notes are inside subfolders (e.g., WhatsApp Voice Notes/202612/)
                    val subfolders = mediaFolder.listFiles()
                    // Sort by last modified to check the newest subfolders first (optimization)
                    val sortedSubfolders = subfolders.filter { it.isDirectory }
                        .sortedByDescending { it.lastModified() }

                    for (subDir in sortedSubfolders) {
                        freshFile = searchInFolder(subDir, mediaType, timestamp)
                        if (freshFile != null) break // Found it!
                    }
                } else {
                    // Standard flat search for Images/Videos
                    freshFile = searchInFolder(mediaFolder, mediaType, timestamp)
                }

                if (freshFile != null) {
                    // ... (keep your existing copy logic)
                    val actualName = freshFile.name ?: fileName
                    val localPath = copyMediaToAppFolder(freshFile, actualName)
                    if (localPath != null) return CopiedMedia(localPath, actualName)
                }
            }
        }
        return null
    }


    /**
     * Searches for file in folder, including by timestamp proximity
     */

    // Inside MediaManager.kt

    private fun searchInFolder(
        folder: DocumentFile,
        mediaType: MediaType,
        timestamp: Long
    ): DocumentFile? {
        val files = folder.listFiles()
        if (files.isEmpty()) return null

        // Fresh media usually appears within 15-30 seconds of the notification
//        val maxTimeDiff = 30_000L

        // Allow files created up to 10 minutes before or AFTER the notification
        // This accounts for slow downloads and clock sync issues.
        val maxTimeDiff = 10 * 60_000L

        var bestMatch: DocumentFile? = null
        var smallestDiff = Long.MAX_VALUE

        // Get valid extensions for this type to filter out junk
        val validExtensions = when (mediaType) {
            MediaType.IMAGE -> IMAGE_EXTENSIONS
            MediaType.VIDEO -> VIDEO_EXTENSIONS
            MediaType.AUDIO -> AUDIO_EXTENSIONS
            else -> emptySet()
        }

        for (file in files) {
            if (!file.isFile) continue

            val fileName = file.name?.lowercase() ?: continue
            val ext = fileName.substringAfterLast(".", "")

            if (validExtensions.isNotEmpty() && !validExtensions.contains(ext)) continue

            val fileTime = file.lastModified()
            val diff = Math.abs(fileTime - timestamp)

            // Only consider files created very close to the notification time
            if (diff < maxTimeDiff && diff < smallestDiff) {
                smallestDiff = diff
                bestMatch = file
            }
        }

        return bestMatch
    }

    private fun findFolderRecursively(
        root: DocumentFile,
        targetFolderName: String,
        maxDepth: Int
    ): DocumentFile? {
        if (maxDepth < 0) return null
        if (!root.isDirectory) return null

        val children = root.listFiles() ?: return null
        for (child in children) {
            if (child.isDirectory && child.name == targetFolderName) {
                return child
            }
        }

        for (child in children) {
            if (child.isDirectory) {
                val found = findFolderRecursively(child, targetFolderName, maxDepth - 1)
                if (found != null) return found
            }
        }
        return null
    }

    /**
     * Checks if files are compatible (same type, similar naming pattern)
     */
    private fun isCompatibleFile(actualFileName: String?, expectedFileName: String): Boolean {
        if (actualFileName == null) return false

        val actualExt = getFileExtension(actualFileName)
        val expectedExt = getFileExtension(expectedFileName)

        // Must have same extension
        if (actualExt != expectedExt) return false

        // Check if it's a WhatsApp media file pattern
        val whatsappPattern = Pattern.compile(
            "(IMG|VID|AUD|PTT|DOC|STK)-\\d{8}-WA\\d{4}\\.",
            Pattern.CASE_INSENSITIVE
        )
        val expectedLooksLikeWhatsAppName = whatsappPattern.matcher(expectedFileName).find()

        // If our expected name is already in WhatsApp format, require the actual file name to match that pattern too.
        // Otherwise (when using a synthetic fallback name), accept any file that has the right extension and WhatsApp-like naming.
        return if (expectedLooksLikeWhatsAppName) {
            whatsappPattern.matcher(actualFileName).find()
        } else {
            whatsappPattern.matcher(actualFileName).find()
        }
    }

    /**
     * Copies media file to app's media folder
     */
    private fun copyMediaToAppFolder(sourceFile: DocumentFile, fileName: String): String? {
        return try {
            val inputStream: InputStream =
                context.contentResolver.openInputStream(sourceFile.uri) ?: return null

            val appMediaFolder = getAppMediaFolder()
            // Always generate a unique destination file name to prevent concurrent notifications
            // from overwriting each other's media (race condition when two coroutines resolve the
            // same synthetic filename at the same time).
            val destinationBase = File(appMediaFolder, fileName)
            val ext = destinationBase.extension.ifBlank { "bin" }
            val base = destinationBase.nameWithoutExtension
            val uniqueSuffix = System.nanoTime()
            val destinationFile = File(appMediaFolder, "${base}-${uniqueSuffix}.$ext")
            Log.d(
                TAG,
                "copyMediaToAppFolder src=${sourceFile.name} reqFileName=$fileName destFile=${destinationFile.name}"
            )

            inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error copying media file: $fileName", e)
            null
        }
    }

    /**
     * Gets or creates app's media folder
     */
    private fun getAppMediaFolder(): File {
        val mediaFolder = File(context.filesDir, "media")
        if (!mediaFolder.exists()) {
            mediaFolder.mkdirs()
        }
        return mediaFolder
    }

    /**
     * Checks if file extension is valid for media
     */
    private fun isValidMediaExtension(fileName: String): Boolean {
        val extension = getFileExtension(fileName)
        return extension in IMAGE_EXTENSIONS ||
                extension in VIDEO_EXTENSIONS ||
                extension in AUDIO_EXTENSIONS ||
                extension in DOCUMENT_EXTENSIONS
    }

    /**
     * Gets file extension from filename
     */
    private fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < fileName.length - 1) {
            fileName.substring(lastDot + 1).lowercase()
        } else ""
    }

    /**
     * Generates fallback media filename based on timestamp and type
     */
    fun generateFallbackFileName(mediaType: MediaType, timestamp: Long): String {
        // Include milliseconds to reduce collisions between multiple notifications in the same second.
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.getDefault())
        val dateStr = dateFormat.format(Date(timestamp))

        val prefix = when (mediaType) {
            MediaType.IMAGE -> "IMG"
            MediaType.VIDEO -> "VID"
            MediaType.AUDIO -> "AUD"
            MediaType.DOCUMENT -> "DOC"
            MediaType.STICKER -> "STK"
            MediaType.GIF -> "GIF"
            else -> "MEDIA"
        }

        val extension = when (mediaType) {
            MediaType.IMAGE -> "jpg"
            MediaType.VIDEO -> "mp4"
            MediaType.AUDIO -> "aac"
            MediaType.DOCUMENT -> "pdf"
            MediaType.STICKER, MediaType.GIF -> "webp"
            else -> "bin"
        }

        return "${prefix}-${dateStr}.${extension}"
    }
}