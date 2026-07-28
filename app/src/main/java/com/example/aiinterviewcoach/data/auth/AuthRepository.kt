package com.example.aiinterviewcoach.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: UserSession?

    fun signInWithEmail(email: String, password: String): Flow<AuthResult<UserSession>>
    fun signUpWithEmail(name: String, email: String, password: String): Flow<AuthResult<UserSession>>
    fun signInWithGoogle(idToken: String): Flow<AuthResult<UserSession>>
    fun signInAsGuest(): Flow<AuthResult<UserSession>>
    fun sendPasswordReset(email: String): Flow<AuthResult<Unit>>
    fun signOut()
}
