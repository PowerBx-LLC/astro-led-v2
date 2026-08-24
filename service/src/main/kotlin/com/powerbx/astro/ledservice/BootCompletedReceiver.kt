package com.powerbx.astro.ledservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Receiver for ACTION_BOOT_COMPLETED.
 * Starts the LED service on device boot.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    private companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Boot completed, starting LED service")

        val serviceIntent = Intent(context, LedService::class.java)

        // API 26+ requires startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
