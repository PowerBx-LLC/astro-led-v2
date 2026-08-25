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
 * Writes commands to sysfs in order: color → effect → power → brightness.
 * (Color must come before power to avoid driver ignoring color after ON.)
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

        // Handle cold-start intent if action is com.powerbx.astro.LED
        if (intent != null && intent.action == "com.powerbx.astro.LED") {
            Log.d(TAG, "Processing cold-start intent")
            broadcastReceiver.onReceive(this, intent)
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
            putExtra("power", if (currentState.power) "ON" else "OFF")
            putExtra("color", currentState.color)
            putExtra("effect", currentState.effect)
            if (currentState.lastError != null) {
                putExtra("lastError", currentState.lastError)
            }
        }
        sendBroadcast(intent)
    }

    /**
     * Broadcast receiver for LED commands.
     * Extras (all optional strings, case-insensitive):
     *   power: "on", "off"
     *   color: color name (RED, GREEN, LIGHT_BLUE, etc., case-insensitive)
     *   effect: "flash", "strobe", "fade", "smooth", "none"
     *   brightness: "up", "down"
     *
     * Execution order: (1) color, (2) effect, (3) power, (4) brightness
     * (Color before power: driver ignores color writes that follow ON)
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
            val power = intent.getStringExtra("power")
            val color = intent.getStringExtra("color")
            val effect = intent.getStringExtra("effect")
            val brightness = intent.getStringExtra("brightness")

            Log.d(
                TAG,
                "LED Command: power=$power, color=$color, effect=$effect, brightness=$brightness"
            )

            // Build new state based on provided values
            val newState = currentState.copy(
                power = if (power != null) power.lowercase() == "on" else currentState.power,
                color = color ?: currentState.color,
                effect = effect ?: currentState.effect,
                lastError = null
            )

            // Apply commands to device
            applyLedState(newState, power, color, effect, brightness)
            updateState(newState)
        }

        private fun handleLedQuery(intent: Intent) {
            Log.d(TAG, "LED Query received")
            val responseIntent = Intent("com.powerbx.astro.LED_STATE").apply {
                putExtra("power", if (currentState.power) "ON" else "OFF")
                putExtra("color", currentState.color)
                putExtra("effect", currentState.effect)
                if (currentState.lastError != null) {
                    putExtra("lastError", currentState.lastError)
                }
            }
            sendBroadcast(responseIntent)
        }

        /**
         * Apply LED state via sequential sysfs writes.
         * Order: (1) color, (2) effect, (3) power, (4) brightness
         * Each field writes its own hex code; skips if not provided.
         * 200ms delay between writes is handled by SysfsWriter.
         */
        private fun applyLedState(
            state: LedState,
            powerStr: String?,
            colorStr: String?,
            effectStr: String?,
            brightnessStr: String?
        ) {
            // (1) Color: write matching color code FIRST (before power)
            if (colorStr != null) {
                val colorCode = parseColorName(colorStr)
                if (colorCode != null) {
                    Log.d(TAG, "Writing color: 0x${String.format("%02X", colorCode)}")
                    SysfsWriter.writeCommand(DeviceProfile.SYSFS_PATH, colorCode)
                } else {
                    Log.w(TAG, "Unknown color: $colorStr")
                }
            }

            // (2) Effect: write effect code or ON for "none"
            if (effectStr != null) {
                val effectCode = parseEffectName(effectStr)
                if (effectCode != null) {
                    Log.d(TAG, "Writing effect: 0x${String.format("%02X", effectCode)}")
                    SysfsWriter.writeCommand(DeviceProfile.SYSFS_PATH, effectCode)
                } else {
                    Log.w(TAG, "Unknown effect: $effectStr")
                }
            }

            // (3) Power: write ON (0x03) or OFF (0x02)
            if (powerStr != null) {
                val powerCode = if (powerStr.lowercase() == "on") {
                    DeviceProfile.Commands.ON
                } else {
                    DeviceProfile.Commands.OFF
                }
                Log.d(TAG, "Writing power: 0x${String.format("%02X", powerCode)}")
                SysfsWriter.writeCommand(DeviceProfile.SYSFS_PATH, powerCode)
            }

            // (4) Brightness: write UP (0x01) or DOWN (0x00)
            if (brightnessStr != null) {
                val brightnessCode = when (brightnessStr.lowercase()) {
                    "up" -> DeviceProfile.Commands.BRIGHTNESS_UP
                    "down" -> DeviceProfile.Commands.BRIGHTNESS_DOWN
                    else -> {
                        Log.w(TAG, "Unknown brightness command: $brightnessStr")
                        null
                    }
                }
                if (brightnessCode != null) {
                    Log.d(TAG, "Writing brightness: 0x${String.format("%02X", brightnessCode)}")
                    SysfsWriter.writeCommand(DeviceProfile.SYSFS_PATH, brightnessCode)
                }
            }
        }

        /**
         * Parse color name (case-insensitive, handles both LIGHT_BLUE and lightBlue).
         * Returns hex code or null if not found.
         */
        private fun parseColorName(name: String): Int? {
            val normalized = name.uppercase()
            return when (normalized) {
                "RED" -> DeviceProfile.Colors.RED
                "GREEN" -> DeviceProfile.Colors.GREEN
                "BLUE" -> DeviceProfile.Colors.BLUE
                "WHITE" -> DeviceProfile.Colors.WHITE
                "RED_ORANGE", "REDORANGE" -> DeviceProfile.Colors.RED_ORANGE
                "MINT" -> DeviceProfile.Colors.MINT
                "PURPLE" -> DeviceProfile.Colors.PURPLE
                "ORANGE" -> DeviceProfile.Colors.ORANGE
                "TURQUOISE" -> DeviceProfile.Colors.TURQUOISE
                "PURPLE_PINK", "PURPLEPINK" -> DeviceProfile.Colors.PURPLE_PINK
                "ORANGE_YELLOW", "ORANGEYELLOW" -> DeviceProfile.Colors.ORANGE_YELLOW
                "LIGHT_BLUE", "LIGHTBLUE" -> DeviceProfile.Colors.LIGHT_BLUE
                "PINK" -> DeviceProfile.Colors.PINK
                "YELLOW" -> DeviceProfile.Colors.YELLOW
                "TEAL" -> DeviceProfile.Colors.TEAL
                "MAGENTA" -> DeviceProfile.Colors.MAGENTA
                else -> null
            }
        }

        /**
         * Parse effect name (case-insensitive).
         * Returns hex code or null if not found.
         */
        private fun parseEffectName(name: String): Int? {
            return when (name.lowercase()) {
                "none" -> DeviceProfile.Commands.ON
                "flash" -> DeviceProfile.Commands.FLASH
                "strobe" -> DeviceProfile.Commands.STROBE
                "fade" -> DeviceProfile.Commands.FADE
                "smooth" -> DeviceProfile.Commands.SMOOTH
                else -> null
            }
        }
    }
}
