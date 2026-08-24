package com.powerbx.astro.led

import android.content.Context
import android.content.Intent
import java.net.HttpURLConnection
import java.net.URL

object AstroLed {
    private const val HTTP_ENDPOINT = "http://127.0.0.1:8188/led"
    private const val HEALTH_ENDPOINT = "http://127.0.0.1:8188/health"
    private const val BROADCAST_ACTION = "com.powerbx.astro.LED"
    private const val TIMEOUT_MS = 3000
    
    private var applicationContext: Context? = null
    private var cachedState: LedState? = null

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    fun isAvailable(): Result<Boolean> = try {
        val available = checkHealthEndpoint()
        Result.Success(available)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    fun on(): Result<Unit> = sendCommand("on", null, null)

    fun off(): Result<Unit> = sendCommand("off", null, null)

    fun setColor(color: Color): Result<Unit> = sendCommand("setColor", color, null)

    fun setEffect(effect: Effect): Result<Unit> = sendCommand("setEffect", null, effect)

    fun brightnessUp(): Result<Unit> = sendCommand("brightnessUp", null, null)

    fun brightnessDown(): Result<Unit> = sendCommand("brightnessDown", null, null)

    fun getState(): Result<LedState> = try {
        val state = fetchState()
        cachedState = state
        Result.Success(state)
    } catch (e: Exception) {
        Result.Failure(e)
    }

    private fun sendCommand(
        command: String,
        color: Color? = null,
        effect: Effect? = null
    ): Result<Unit> {
        return try {
            val success = try {
                sendHttpCommand(command, color, effect)
            } catch (e: Exception) {
                // Fallback to Intent broadcast
                sendBroadcastCommand(command, color, effect)
                true
            }
            if (success) {
                Result.Success(Unit)
            } else {
                Result.Failure(Exception("Command execution failed: $command"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    private fun sendHttpCommand(
        command: String,
        color: Color? = null,
        effect: Effect? = null
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

            val payload = buildJsonPayload(command, color, effect)
            connection.outputStream.write(payload.toByteArray())
            connection.outputStream.flush()

            val responseCode = connection.responseCode
            responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

    private fun sendBroadcastCommand(
        command: String,
        color: Color? = null,
        effect: Effect? = null
    ): Boolean {
        val context = applicationContext ?: return false
        
        val intent = Intent(BROADCAST_ACTION).apply {
            putExtra("command", command)
            color?.let { putExtra("color", it.name) }
            effect?.let { putExtra("effect", it.name) }
        }
        
        context.sendBroadcast(intent)
        return true
    }

    private fun buildJsonPayload(
        command: String,
        color: Color? = null,
        effect: Effect? = null
    ): String {
        val parts = mutableListOf("\"command\":\"$command\"")
        color?.let { parts.add("\"color\":\"${it.name}\"") }
        effect?.let { parts.add("\"effect\":\"${it.name}\"") }
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
        val url = URL("$HTTP_ENDPOINT/state")
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

            // Simple JSON parsing (no dependency on external libraries)
            if (json.contains("\"power\":\"ON\"")) {
                power = Power.ON
            }

            Color.values().forEach { c ->
                if (json.contains("\"color\":\"${c.name}\"")) {
                    color = c
                }
            }

            Effect.values().forEach { e ->
                if (json.contains("\"effect\":\"${e.name}\"")) {
                    effect = e
                }
            }

            LedState(power, color, effect)
        } catch (e: Exception) {
            LedState(Power.OFF, lastError = e.message)
        }
    }
}
