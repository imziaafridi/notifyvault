package com.ziaafridi.notifyvault

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Log

/**
 * Service to scan and recover media files for deleted WhatsApp messages.
 */
class DeletedMessageRecoveryService : Service() {
    private val tag = "DeletedRecoveryService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand startId=$startId")

        val notification = buildOngoingNotification()

        // Required for Android 10+ background execution
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1002,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1002, notification)
        }
     //   startForeground(1002, buildOngoingNotification())
        serviceScope.launch {
            try {
                recoverDeletedMessagesMissingMedia()
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun recoverDeletedMessagesMissingMedia() = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val mediaManager = MediaManager(applicationContext)
        // Fetch messages marked as deleted that are still missing local media files
        val pending = db.messageDao().getDeletedMessagesWithoutMedia()
        if (pending.isEmpty()) {
            Log.d(tag, "No deleted messages pending media recovery")
            return@withContext
        }

        Log.d(tag, "Pending deleted messages without media: ${pending.size}")

        for (message in pending) {
            // Skip messages younger than 500ms to avoid race conditions
            val age = System.currentTimeMillis() - message.timestamp
            if (age < 500) continue

            val needsRecovery = message.mediaPath.isNullOrBlank() || message.mediaPath.let { path -> !File(path).exists() }

            if (!needsRecovery) continue

            Log.d(
                tag,
                "Recovering messageId=${message.id} sender=${message.sender} ts=${message.timestamp} mediaType=${message.mediaType}"
            )

            val hintType = message.mediaType
                ?.let { raw ->
                    runCatching { MediaManager.MediaType.valueOf(raw) }.getOrNull()
                }
            // Search for the file in WhatsApp's internal storage/cache
            val recovered = if (hintType != null) {
                mediaManager.findAndCopyMediaByType(
                    mediaType = hintType,
                    fileNameHint = message.mediaFileName,
                    timestamp = message.timestamp
                )
            } else {
                mediaManager.findAndCopyMedia(
                    message = message.message,
                    timestamp = message.timestamp
                )
            }
            // Save the new path to the database if the file was found
            if (recovered.isAvailable && !recovered.localPath.isNullOrBlank()) {
                Log.d(tag, "Recovered media for messageId=${message.id} -> ${recovered.localPath}")
                db.messageDao().update(
                    message.copy(
                        mediaPath = recovered.localPath,
                        mediaType = if (recovered.type != MediaManager.MediaType.UNKNOWN) recovered.type.name else message.mediaType,
                        mediaFileName = recovered.fileName
                    )
                )
            } else {
                Log.d(tag, "Recovery not available for messageId=${message.id}")
            }
        }
    }

    private fun buildOngoingNotification(): Notification {
        val channelId = DeletedMessageRecoveryNotification.CHANNEL_ID

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Recovering deleted WhatsApp messages")
            .setContentText("Restoring missing media (if available)...")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            DeletedMessageRecoveryNotification.CHANNEL_ID,
            DeletedMessageRecoveryNotification.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    private object DeletedMessageRecoveryNotification {
        const val CHANNEL_ID = "deleted_message_recovery"
        const val CHANNEL_NAME = "Deleted message recovery"
    }
}

