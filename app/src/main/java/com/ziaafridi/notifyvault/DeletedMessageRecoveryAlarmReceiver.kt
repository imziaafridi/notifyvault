package com.ziaafridi.notifyvault
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Triggers the background recovery service when a scheduled retry alarm fires.
 */
class DeletedMessageRecoveryAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("DeletedRecoveryAlarm", "onReceive action=${intent?.action}")
        val appContext = context.applicationContext

        // Increment attempt index and start recovery service
        val currentAttemptIndex = DeletedMessageRecoveryScheduler.getAndIncrementAttemptIndex(appContext)
        Log.d("DeletedRecoveryAlarm", "Starting attempt index: $currentAttemptIndex")

        val serviceIntent = Intent(appContext, DeletedMessageRecoveryService::class.java)
        appContext.startForegroundService(serviceIntent)

        // Schedule the next retry interval
        DeletedMessageRecoveryScheduler.scheduleNext(appContext, currentAttemptIndex + 1)

    }
}

