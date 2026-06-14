package com.khata.app.core.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A generic class that holds a value with its loading status.
 * @param <out T>
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Cannot get value from Loading state")
    }
}

/**
 * Extension function to convert a Flow of T to a Flow of Result<T>
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { emit(Result.Error(it, it.message ?: "Unknown error occurred")) }
}

/**
 * Functional interface for handling success state
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Functional interface for handling error state
 */
inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) action(this)
    return this
}

/**
 * Helper function for safe database/repository calls
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e, e.message ?: "An unexpected error occurred")
    }
}
