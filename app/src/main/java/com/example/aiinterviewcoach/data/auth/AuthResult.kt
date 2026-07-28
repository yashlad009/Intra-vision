package com.example.aiinterviewcoach.data.auth

/**
 * Sealed class representing authentication operation states.
 */
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}
