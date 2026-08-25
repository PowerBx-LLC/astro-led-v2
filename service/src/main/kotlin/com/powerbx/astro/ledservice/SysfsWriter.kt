package com.powerbx.astro.ledservice

import android.util.Log
import java.io.File
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

        // Check if sysfs node exists
        if (!File(devicePath).exists()) {
            Log.e(TAG, "Sysfs node not found: $devicePath")
            return Result.Failure(ErrorCode.ERR_NODE_MISSING)
        }

        val hexValue = String.format("%02X", command)
        val echoCommand = "echo w 0x$hexValue > $devicePath"

        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "0", "sh", "-c", echoCommand))
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                Log.d(TAG, "Command 0x$hexValue written successfully to $devicePath")
                Result.Success(Unit)
            } else {
                Log.e(TAG, "Write failed with exit code $exitCode")
                Result.Failure(ErrorCode.ERR_WRITE_FAILED)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception executing sysfs write: ${e.message}", e)
            when {
                e.message?.contains("Permission denied") == true -> 
                    Result.Failure(ErrorCode.ERR_ROOT_DENIED)
                e.message?.contains("su:") == true -> 
                    Result.Failure(ErrorCode.ERR_ROOT_DENIED)
                else -> 
                    Result.Failure(ErrorCode.ERR_WRITE_FAILED)
            }
        }
    }
}
