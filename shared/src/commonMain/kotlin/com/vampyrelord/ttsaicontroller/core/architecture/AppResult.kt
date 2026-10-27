package com.vampyrelord.ttsaicontroller.core.architecture

/**
 * Universal functional Result wrapper for Clean Architecture Use Cases and Repositories.
 */
sealed interface AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>
    data class Error(val throwable: Throwable? = null, val message: String = throwable?.message ?: "Unknown error") : AppResult<Nothing>
    data object Loading : AppResult<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default()
    }
}
