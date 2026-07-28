package com.example.aiinterviewcoach.data.auth

data class UserSession(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val isGuest: Boolean = false
)
