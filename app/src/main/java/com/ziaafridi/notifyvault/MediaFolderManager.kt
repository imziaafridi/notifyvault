package com.ziaafridi.notifyvault

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MediaFolderManager(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("media_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val WHATSAPP_FOLDER_URI_KEY = "whatsapp_folder_uri"
    }
    
    fun getWhatsAppFolderUri(): Uri? {
        val uriString = prefs.getString(WHATSAPP_FOLDER_URI_KEY, null)
        return if (uriString != null) Uri.parse(uriString) else null
    }
    
    fun setWhatsAppFolderUri(uri: Uri) {
        prefs.edit().putString(WHATSAPP_FOLDER_URI_KEY, uri.toString()).apply()
    }
    
    fun hasWhatsAppFolderAccess(): Boolean {
        val uri = getWhatsAppFolderUri()
        return uri != null && isUriAccessible(uri)
    }
    
    private fun isUriAccessible(uri: Uri): Boolean {
        return try {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
            documentFile?.exists() == true && documentFile.canRead()
        } catch (e: Exception) {
            false
        }
    }
    
    fun copyMediaFromWhatsApp(fileName: String): Boolean {
        val whatsappUri = getWhatsAppFolderUri() ?: return false
        
        try {
            val whatsappFolder = DocumentFile.fromTreeUri(context, whatsappUri) ?: return false
            
            // Look for the file in WhatsApp Media folders
            val mediaFolders = listOf("WhatsApp Images", "WhatsApp Video", "WhatsApp Audio", "WhatsApp Documents")
            
            for (folderName in mediaFolders) {
                val mediaFolder = whatsappFolder.findFile(folderName)
                if (mediaFolder != null && mediaFolder.exists()) {
                    val file = findFileRecursively(mediaFolder, fileName)
                    if (file != null) {
                        return copyFileToAppFolder(file, fileName)
                    }
                }
            }
            
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun findFileRecursively(folder: DocumentFile, fileName: String): DocumentFile? {
        try {
            // First check direct children
            folder.listFiles().forEach { file ->
                if (file.name == fileName) {
                    return file
                }
            }
            
            // Then check subdirectories
            folder.listFiles().forEach { file ->
                if (file.isDirectory) {
                    val found = findFileRecursively(file, fileName)
                    if (found != null) return found
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }
    
    private fun copyFileToAppFolder(sourceFile: DocumentFile, fileName: String): Boolean {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(sourceFile.uri) ?: return false

            val bufferedInputStream = java.io.BufferedInputStream(inputStream)

            // Create app's media folder if it doesn't exist
            val appMediaFolder = File(context.filesDir, "media")
            if (!appMediaFolder.exists()) {
                appMediaFolder.mkdirs()
            }
            
            val destinationFile = File(appMediaFolder, fileName)
            val outputStream = FileOutputStream(destinationFile)

            bufferedInputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getAppMediaFolder(): File {
        val mediaFolder = File(context.filesDir, "media")
        if (!mediaFolder.exists()) {
            mediaFolder.mkdirs()
        }
        return mediaFolder
    }
    
    fun listCopiedMediaFiles(): List<File> {
        val mediaFolder = getAppMediaFolder()
        return mediaFolder.listFiles()?.toList() ?: emptyList()
    }
    
    fun clearMediaFolder() {
        val mediaFolder = getAppMediaFolder()
        mediaFolder.listFiles()?.forEach { file ->
            file.delete()
        }
    }
}