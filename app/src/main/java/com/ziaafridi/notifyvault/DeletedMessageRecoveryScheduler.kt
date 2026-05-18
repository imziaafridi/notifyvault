package com.ziaafridi.notifyvault

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit

/**
 * Manages the scheduling and retry logic for recovering deleted WhatsApp media.
 */
object DeletedMessageRecoveryScheduler {

    private const val PREFS = "deleted_message_recovery_prefs"
    private const val KEY_ATTEMPTS_LEFT = "attempts_left"
    private const val KEY_ATTEMPTS_DONE = "attempts_done" // Track how many we've finished

    const val INTERVAL_MS: Long = 60_000L // 1 minute
    private const val MAX_ATTEMPTS: Int = 6 // 6 minutes total

    private const val REQUEST_CODE: Int = 234324242

    // interval logic
    val intervals = listOf(
        15_000L,  // 15 seconds
        30_000L,  // 30 seconds
        60_000L,  // 1 minute
        120_000L, // 2 minutes
        120_000L, // 2 minutes
        300_000L  // 5 minutes
    )

    // Start the recovery sequence from the beginning
    fun schedule(context: Context) {
        val appContext = context.applicationContext

        // Reset attempts to 0 when a new recovery starts
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_ATTEMPTS_DONE, 0) }

        scheduleNext(appContext, 0)
    }

    // Schedule the next alarm based on the current attempt index
    fun scheduleNext(context: Context, attemptIndex: Int) {
        if (attemptIndex >= intervals.size) {
            Log.d("DeletedRecovery", "All recovery attempts finished.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DeletedMessageRecoveryAlarmReceiver::class.java).apply {
            action = "com.ziaafridi.notifyvault.DELETED_MESSAGE_RECOVERY_ALARM"
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

        val delay = intervals[attemptIndex]
        val triggerAt = System.currentTimeMillis() + delay

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)

        Log.d("DeletedRecovery", "Scheduled attempt #$attemptIndex in ${delay/1000}s")
    }

    // Get the current attempt count and increment it for the next run
    fun getAndIncrementAttemptIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_ATTEMPTS_DONE, 0)
        prefs.edit { putInt(KEY_ATTEMPTS_DONE, current + 1) }
        return current
    }

}

