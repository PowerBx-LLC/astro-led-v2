package com.powerbx.astro.ledservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/**
 * Foreground service managing LED state and handling broadcast commands.
 */
class LedService : Service() {
    private companion object {
        private const val TAG = "LedService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "com.powerbx.astro.ledservice.channel"
    }

    private var currentState: LedState = LedState()
    private val broadcastReceiver = LedCommandReceiver()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        currentState = LedState.restore(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        // Start as foreground service with notification
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Register broadcast receiver
        val intentFilter = IntentFilter().apply {
            addAction("com.powerbx.astro.LED")
            addAction("com.powerbx.astro.LED_QUERY")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                this,
                broadcastReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(broadcastReceiver, intentFilter)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister receiver", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ASTRO LED Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "LED control service for ASTRO"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ASTRO LED Service")
            .setContentText("LED control active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun updateState(newState: LedState) {
        currentState = newState
        currentState.save(this)
        broadcastStateChange()
    }

    private fun broadcastStateChange() {
        val intent = Intent("com.powerbx.astro.LED_STATE").apply {
            putExtra("state", currentState.toJson().toString())
        }
        sendBroadcast(intent)
    }

    /**
     * Broadcast receiver for LED commands.
     * Expected extras: power (boolean), color (String), effect (String), brightness (Int)
     * in that order.
     */
    inner class LedCommandReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            Log.d(TAG, "Broadcast received: ${intent.action}")

            when (intent.action) {
                "com.powerbx.astro.LED" -> handleLedCommand(intent)
                "com.powerbx.astro.LED_QUERY" -> handleLedQuery(intent)
            }
        }

        private fun handleLedCommand(intent: Intent) {
            // Extract extras in order: power, color, effect, brightness
            val power = intent.getBooleanExtra("power", currentState.power)
            val color = intent.getStringExtra("color") ?: currentState.color
            val effect = intent.getStringExtra("effect") ?: currentState.effect
            val brightness = intent.getIntExtra("brightness", currentState.brightness)

            Log.d(
                TAG,
                "LED Command: power=$power, color=$color, effect=$effect, brightness=$brightness"
            )

            val newState = LedState(
                power = power,
                color = color,
                effect = effect,
                brightness = brightness
            )

            // Apply command to device
            applyLedState(newState)
            updateState(newState)
        }

        private fun handleLedQuery(intent: Intent) {
            Log.d(TAG, "LED Query received")
            val responseIntent = Intent("com.powerbx.astro.LED_STATE").apply {
                putExtra("state", currentState.toJson().toString())
            }
            sendBroadcast(responseIntent)
        }

        private fun applyLedState(state: LedState) {
            // Construct command code from state
            var command = 0x00

            // Power bit
            if (state.power) {
                command = command or 0x01
            }

            // Effect bits (offset by 1)
            val effectCode = when (state.effect.uppercase()) {
                "STATIC" -> 0x00
                "PULSE" -> 0x01
                "STROBE" -> 0x02
                "FADE" -> 0x03
                "RAINBOW" -> 0x04
                else -> 0x00
            }
            command = command or (effectCode shl 1)

            // Color bits (offset by 4)
            val colorCode = DeviceProfile.ColorCode.fromName(state.color)?.value ?: 0x06
            command = command or (colorCode shl 4)

            Log.d(TAG, "Applying command: 0x${String.format("%02X", command)}")
            SysfsWriter.writeCommandAsync(
                DeviceProfile.SYSFS_PATH,
                command
            ) { result ->
                when (result) {
                    is SysfsWriter.Result.Success -> {
                        Log.d(TAG, "LED command applied successfully")
                    }

                    is SysfsWriter.Result.Failure -> {
                        Log.e(TAG, "LED command failed: ${result.error.code}")
                        updateState(currentState.copy(lastError = result.error.code))
                    }
                }
            }
        }
    }
}
