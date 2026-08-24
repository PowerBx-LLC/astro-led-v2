package com.powerbx.astro.led

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure<T>(val error: Throwable) : Result<T>()

    fun onSuccess(block: (T) -> Unit): Result<T> {
        if (this is Success) {
            block(data)
        }
        return this
    }

    fun onFailure(block: (Throwable) -> Unit): Result<T> {
        if (this is Failure) {
            block(error)
        }
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> error
    }
}
