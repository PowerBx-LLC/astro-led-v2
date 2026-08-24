package com.powerbx.astro.ledservice

import android.content.Context
import org.json.JSONObject

/**
 * Immutable LED state with SharedPreferences persistence.
 * Fields: power (ON/OFF), color, effect (none/flash/strobe/fade/smooth), lastError
 */
data class LedState(
    val power: Boolean = false,
    val color: String = "WHITE",
    val effect: String = "none",
    val lastError: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("power", if (power) "ON" else "OFF")
        put("color", color)
        put("effect", effect)
        if (lastError != null) {
            put("lastError", lastError)
        }
    }

    companion object {
        private const val PREFS_NAME = "com.powerbx.astro.ledservice.state"
        private const val KEY_POWER = "power"
        private const val KEY_COLOR = "color"
        private const val KEY_EFFECT = "effect"
        private const val KEY_LAST_ERROR = "lastError"

        fun fromJson(json: JSONObject): LedState {
            return LedState(
                power = json.optBoolean(KEY_POWER, false),
                color = json.optString(KEY_COLOR, "WHITE"),
                effect = json.optString(KEY_EFFECT, "none"),
                lastError = json.optString(KEY_LAST_ERROR, null).ifEmpty { null }
            )
        }

        fun load(context: Context): LedState {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return LedState(
                power = prefs.getBoolean(KEY_POWER, false),
                color = prefs.getString(KEY_COLOR, "WHITE") ?: "WHITE",
                effect = prefs.getString(KEY_EFFECT, "none") ?: "none",
                lastError = prefs.getString(KEY_LAST_ERROR, null)
            )
        }

        fun restore(context: Context): LedState {
            return load(context)
        }
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_POWER, power)
            putString(KEY_COLOR, color)
            putString(KEY_EFFECT, effect)
            if (lastError != null) {
                putString(KEY_LAST_ERROR, lastError)
            } else {
                remove(KEY_LAST_ERROR)
            }
            apply()
        }
    }
}
