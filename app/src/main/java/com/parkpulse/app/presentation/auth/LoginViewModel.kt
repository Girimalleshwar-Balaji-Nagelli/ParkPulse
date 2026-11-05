package com.parkpulse.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.domain.repository.AuthRepository
import com.parkpulse.app.domain.usecase.LoginResult
import com.parkpulse.app.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Pre-fill email if the user ticked "Remember me" last time.
        viewModelScope.launch {
            val saved = authRepository.rememberedEmail.first()
            if (saved.isNotBlank()) {
                _uiState.update { if (it.email.isBlank()) it.copy(email = saved, rememberMe = true) else it }
            }
        }
    }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onRememberMeChange(value: Boolean) =
        _uiState.update { it.copy(rememberMe = value) }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.update { it.copy(isLoading = true, emailError = null, passwordError = null, generalError = null) }

        viewModelScope.launch {
            when (val result = loginUseCase(state.email, state.password, state.rememberMe)) {
                LoginResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.NavigateToHome)
                }
                LoginResult.InvalidCredentials -> _uiState.update {
                    it.copy(isLoading = false, generalError = "Incorrect email or password")
                }
                is LoginResult.ValidationError -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailError = result.emailError,
                        passwordError = result.passwordError
                    )
                }
            }
        }
    }
}
