package com.powerbx.astro.ledservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Exported broadcast receiver for com.powerbx.astro.LED and com.powerbx.astro.LED_QUERY actions.
 * This receiver forwards commands to the LedService.
 */
class LedBroadcastReceiver : BroadcastReceiver() {
    private companion object {
        private const val TAG = "LedBroadcastReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }

        Log.d(TAG, "Broadcast received: ${intent.action}")

        // Forward to service
        val serviceIntent = Intent(context, LedService::class.java).apply {
            action = intent.action
            putExtras(intent)
        }

        androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
    }
}
