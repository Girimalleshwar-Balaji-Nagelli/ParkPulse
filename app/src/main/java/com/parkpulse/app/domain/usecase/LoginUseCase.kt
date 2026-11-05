package com.parkpulse.app.domain.usecase

import android.util.Patterns
import com.parkpulse.app.domain.repository.AuthRepository
import javax.inject.Inject

sealed interface LoginResult {
    data object Success : LoginResult
    data object InvalidCredentials : LoginResult
    data class ValidationError(val emailError: String?, val passwordError: String?) : LoginResult
}

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, rememberMe: Boolean): LoginResult {
        val trimmed = email.trim()
        val emailError = when {
            trimmed.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Enter a valid email address"
            else -> null
        }
        val passwordError = if (password.isEmpty()) "Password is required" else null

        if (emailError != null || passwordError != null) {
            return LoginResult.ValidationError(emailError, passwordError)
        }
        return if (repository.login(trimmed, password, rememberMe)) {
            LoginResult.Success
        } else {
            LoginResult.InvalidCredentials
        }
    }
}
