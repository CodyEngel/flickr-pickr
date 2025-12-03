package dev.engel.flickrpickr.core.data.network

import kotlinx.coroutines.delay
import okio.IOException
import retrofit2.HttpException

suspend fun <T> retryWithExponentialBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 10000,
    shouldRetry: (Throwable) -> Boolean = { it.isRetryable() },
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    repeat(maxRetries - 1) { _ ->
        try {
            return block()
        } catch (e: Exception) {
            if (!shouldRetry(e)) throw e
        }
        delay(currentDelay)
        currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
    }
    return block()
}

fun Throwable.isRetryable(): Boolean = when (this) {
    is IOException -> true
    is HttpException -> code() in 500..599 || code() == 429
    else -> false
}