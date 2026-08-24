package com.powerbx.astro.ledservice

import android.util.Log
import java.util.concurrent.Executors

/**
 * Single-threaded sysfs writer for LED control.
 * Handles root escalation and error handling.
 */
object SysfsWriter {
    private const val TAG = "SysfsWriter"
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable).apply { name = "SysfsWriter" }
    }

    sealed class ErrorCode(val code: String) {
        object ERR_ROOT_DENIED : ErrorCode("ERR_ROOT_DENIED")
        object ERR_NODE_MISSING : ErrorCode("ERR_NODE_MISSING")
        object ERR_WRITE_FAILED : ErrorCode("ERR_WRITE_FAILED")
        object ERR_UNSUPPORTED_DEVICE : ErrorCode("ERR_UNSUPPORTED_DEVICE")
        object ERR_BAD_ARG : ErrorCode("ERR_BAD_ARG")
    }

    sealed class Result<out T> {
        data class Success<T>(val value: T) : Result<T>()
        data class Failure(val error: ErrorCode) : Result<Nothing>()
    }

    /**
     * Write a command to sysfs via root.
     * Format: su -c "echo w 0x{hex} > /sys/..."
     */
    fun writeCommand(devicePath: String, command: Int): Result<Unit> {
        if (command < 0x00 || command > 0xFF) {
            return Result.Failure(ErrorCode.ERR_BAD_ARG)
        }

        val hexValue = String.format("%02X", command)
        val echoCommand = "echo w 0x$hexValue > $devicePath"
        val fullCommand = "su -c \"$echoCommand\""

        return try {
            Log.d(TAG, "Executing: $fullCommand")
            val process = Runtime.getRuntime().exec(fullCommand)
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Log.d(TAG, "Write successful: 0x$hexValue to $devicePath")
                Result.Success(Unit)
            } else {
                val errorStream = process.errorStream.bufferedReader().use { it.readText() }
                Log.e(TAG, "Command failed with exit code $exitCode: $errorStream")

                when {
                    errorStream.contains("denied", ignoreCase = true) ||
                    errorStream.contains("permission", ignoreCase = true) ->
                        Result.Failure(ErrorCode.ERR_ROOT_DENIED)

                    errorStream.contains("no such file", ignoreCase = true) ||
                    errorStream.contains("not found", ignoreCase = true) ->
                        Result.Failure(ErrorCode.ERR_NODE_MISSING)

                    else -> Result.Failure(ErrorCode.ERR_WRITE_FAILED)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during write", e)
            return when {
                e.message?.contains("No such file", ignoreCase = true) == true ->
                    Result.Failure(ErrorCode.ERR_NODE_MISSING)

                else -> Result.Failure(ErrorCode.ERR_WRITE_FAILED)
            }
        }
    }

    /**
     * Async write with callback.
     */
    fun writeCommandAsync(
        devicePath: String,
        command: Int,
        onResult: (Result<Unit>) -> Unit
    ) {
        executor.execute {
            val result = writeCommand(devicePath, command)
            onResult(result)
        }
    }

    /**
     * Verify device is reachable.
     */
    fun isDeviceReachable(devicePath: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("test -e $devicePath")
            process.waitFor() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check device", e)
            false
        }
    }
}
