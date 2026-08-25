package com.powerbx.astro.ledservice

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

/**
 * HTTP server for LED control on loopback interface (127.0.0.1:8188).
 * GET /led - retrieve current LED state as JSON
 * POST /led - update LED state from JSON body
 * GET /health - return device profile info
 */
class LedHttpServer(
    private val port: Int = 8188,
    private val onStateChange: (LedState) -> Unit
) : NanoHTTPD("127.0.0.1", port) {
    private companion object {
        private const val TAG = "LedHttpServer"
    }

    private var currentState: LedState = LedState()

    init {
        Log.d(TAG, "HTTP server initialized on 127.0.0.1:$port")
    }

    fun setState(state: LedState) {
        currentState = state
    }

    override fun serve(session: IHTTPSession?): Response {
        if (session == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Invalid request")
        }

        Log.d(TAG, "${session.method} ${session.uri}")

        return when {
            session.uri == "/led" && session.method == Method.GET -> handleGetLed()
            session.uri == "/led" && session.method == Method.POST -> handlePostLed(session)
            session.uri == "/health" && session.method == Method.GET -> handleGetHealth()
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "application/json",
                JSONObject().put("error", "Not found").toString()
            )
        }
    }

    private fun handleGetLed(): Response {
        return try {
            val stateJson = currentState.toJson().toString()
            newFixedLengthResponse(Response.Status.OK, "application/json", stateJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error serializing state", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                JSONObject().put("error", e.message).toString()
            )
        }
    }

    private fun handlePostLed(session: IHTTPSession): Response {
        return try {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val body = files["postData"] ?: ""
            if (body.isBlank()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    "application/json",
                    JSONObject().put("error", "Empty body").toString()
                )
            }
            val json = JSONObject(body)

            val powerStr = json.optString("power", "")
            val newState = LedState(
                power = when (powerStr.lowercase()) {
                    "on", "true" -> true
                    "off", "false" -> false
                    else -> currentState.power
                },
                color = json.optString("color", currentState.color),
                effect = json.optString("effect", currentState.effect)
            )

            Log.d(TAG, "Applying state from HTTP: $newState")
            onStateChange(newState)
            currentState = newState

            val responseJson = JSONObject().put("success", true).put("state", newState.toJson())
            newFixedLengthResponse(Response.Status.OK, "application/json", responseJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error processing POST", e)
            newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json",
                JSONObject().put("error", e.message).toString()
            )
        }
    }

    private fun handleGetHealth(): Response {
        return try {
            val healthJson = JSONObject().apply {
                put("status", "ok")
                put("device", DeviceProfile.DEVICE_NAME)
                put("sysfsPath", DeviceProfile.SYSFS_PATH)
                put("reachable", java.io.File(DeviceProfile.SYSFS_PATH).exists())
            }
            newFixedLengthResponse(Response.Status.OK, "application/json", healthJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error in health check", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                JSONObject().put("error", e.message).toString()
            )
        }
    }
}
