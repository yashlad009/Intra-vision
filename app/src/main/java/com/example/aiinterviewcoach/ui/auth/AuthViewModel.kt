package com.example.aiinterviewcoach.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.auth.AuthRepository
import com.example.aiinterviewcoach.data.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object PasswordResetSent : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    val isLoggedIn: Boolean
        get() = authRepository.currentUser != null

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both email and password.")
            return
        }
        viewModelScope.launch {
            authRepository.signInWithEmail(email, password).collect { result ->
                _uiState.value = when (result) {
                    is AuthResult.Loading -> AuthUiState.Loading
                    is AuthResult.Success -> AuthUiState.Success(result.data)
                    is AuthResult.Error -> AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, password: String, passwordConfirm: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all required fields.")
            return
        }
        if (password != passwordConfirm) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long.")
            return
        }
        viewModelScope.launch {
            authRepository.signUpWithEmail(name, email, password).collect { result ->
                _uiState.value = when (result) {
                    is AuthResult.Loading -> AuthUiState.Loading
                    is AuthResult.Success -> AuthUiState.Success(result.data)
                    is AuthResult.Error -> AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).collect { result ->
                _uiState.value = when (result) {
                    is AuthResult.Loading -> AuthUiState.Loading
                    is AuthResult.Success -> AuthUiState.Success(result.data)
                    is AuthResult.Error -> AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            authRepository.signInAsGuest().collect { result ->
                _uiState.value = when (result) {
                    is AuthResult.Loading -> AuthUiState.Loading
                    is AuthResult.Success -> AuthUiState.Success(result.data)
                    is AuthResult.Error -> AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your email address to reset your password.")
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email).collect { result ->
                _uiState.value = when (result) {
                    is AuthResult.Loading -> AuthUiState.Loading
                    is AuthResult.Success -> AuthUiState.PasswordResetSent
                    is AuthResult.Error -> AuthUiState.Error(result.message)
                }
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
