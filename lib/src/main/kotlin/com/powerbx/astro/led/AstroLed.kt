package com.powerbx.astro.led

import android.content.Context
import android.content.Intent
import java.net.HttpURLConnection
import java.net.URL

object AstroLed {
    private const val HTTP_ENDPOINT = "http://127.0.0.1:8188/led"
    private const val HEALTH_ENDPOINT = "http://127.0.0.1:8188/health"
    private const val BROADCAST_ACTION = "com.powerbx.astro.LED"
    private const val SERVICE_PACKAGE = "com.powerbx.astro.ledservice"
    private const val TIMEOUT_MS = 3000

    fun isAvailable(context: Context): Boolean = try {
        checkHealthEndpoint()
    } catch (e: Exception) {
        false
    }

    fun on(context: Context): Result<Unit> = sendCommand(context, "on", null, null, null)

    fun off(context: Context): Result<Unit> = sendCommand(context, "off", null, null, null)

    fun setColor(context: Context, color: Color): Result<Unit> = 
        sendCommand(context, null, color.name.lowercase(), null, null)

    fun setEffect(context: Context, effect: Effect): Result<Unit> = 
        sendCommand(context, null, null, effect.name.lowercase(), null)

    fun brightnessUp(context: Context): Result<Unit> = 
        sendCommand(context, null, null, null, "up")

    fun brightnessDown(context: Context): Result<Unit> = 
        sendCommand(context, null, null, null, "down")

    fun getState(context: Context): Result<LedState> = try {
        val state = fetchState()
        Result.Success(state)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    private fun sendCommand(
        context: Context,
        power: String?,
        color: String?,
        effect: String?,
        brightness: String?
    ): Result<Unit> {
        return try {
            val success = try {
                sendHttpCommand(power, color, effect, brightness)
            } catch (e: Exception) {
                // Fallback to Intent broadcast
                sendBroadcastCommand(context, power, color, effect, brightness)
                true
            }
            if (success) {
                Result.Success(Unit)
            } else {
                Result.Failure(Exception("Command execution failed"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    private fun sendHttpCommand(
        power: String?,
        color: String?,
        effect: String?,
        brightness: String?
    ): Boolean {
        val url = URL(HTTP_ENDPOINT)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.apply {
                requestMethod = "POST"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val payload = buildJsonPayload(power, color, effect, brightness)
            connection.outputStream.write(payload.toByteArray())
            connection.outputStream.flush()

            val responseCode = connection.responseCode
            responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

    private fun sendBroadcastCommand(
        context: Context,
        power: String?,
        color: String?,
        effect: String?,
        brightness: String?
    ): Boolean {
        val intent = Intent(BROADCAST_ACTION).apply {
            power?.let { putExtra("power", it) }
            color?.let { putExtra("color", it) }
            effect?.let { putExtra("effect", it) }
            brightness?.let { putExtra("brightness", it) }
            setPackage(SERVICE_PACKAGE)
        }

        context.sendBroadcast(intent)
        return true
    }

    private fun buildJsonPayload(
        power: String?,
        color: String?,
        effect: String?,
        brightness: String?
    ): String {
        val parts = mutableListOf<String>()
        power?.let { parts.add("\"power\":\"$it\"") }
        color?.let { parts.add("\"color\":\"$it\"") }
        effect?.let { parts.add("\"effect\":\"$it\"") }
        brightness?.let { parts.add("\"brightness\":\"$it\"") }
        return "{${parts.joinToString(",")}}"
    }

    private fun checkHealthEndpoint(): Boolean {
        val url = URL(HEALTH_ENDPOINT)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            val responseCode = connection.responseCode
            responseCode in 200..299
        } catch (e: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchState(): LedState {
        val url = URL(HTTP_ENDPOINT)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }

            if (connection.responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseStateResponse(response)
            } else {
                LedState(Power.OFF, lastError = "HTTP ${connection.responseCode}")
            }
        } catch (e: Exception) {
            LedState(Power.OFF, lastError = e.message)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseStateResponse(json: String): LedState {
        return try {
            var power = Power.OFF
            var color: Color? = null
            var effect: Effect? = null

            // Simple JSON parsing (no external dependency)
            if (json.contains("\"power\":\"ON\"", ignoreCase = true)) {
                power = Power.ON
            }

            Color.values().forEach { c ->
                if (json.contains("\"color\":\"${c.name}\"", ignoreCase = true)) {
                    color = c
                }
            }

            Effect.values().forEach { e ->
                if (json.contains("\"effect\":\"${e.name}\"", ignoreCase = true)) {
                    effect = e
                }
            }

            LedState(power, color, effect)
        } catch (e: Exception) {
            LedState(Power.OFF, lastError = e.message)
        }
    }
}
