package com.example.myapplication.ui.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.AuthRepository
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState = mutableStateOf(LoginUiState())

    fun onEmailChange(value: String) {
        uiState.value = uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        uiState.value = uiState.value.copy(password = value, error = null)
    }

    fun login(onSuccess: () -> Unit) {
        val currentState = uiState.value

        val emailLimpo = currentState.email.trim()
        val passwordLimpa = currentState.password.trim()

        android.util.Log.d(
            "IOT_ROOM_DEBUG",
            "LOGIN FORM: email='$emailLimpo' passwordLength=${currentState.password.length} passwordTrimmedLength=${passwordLimpa.length}"
        )

        if (emailLimpo.isBlank() || passwordLimpa.isBlank()) {
            uiState.value = currentState.copy(
                error = "Preenche email e password."
            )
            return
        }

        viewModelScope.launch {
            uiState.value = currentState.copy(
                loading = true,
                error = "A tentar fazer login..."
            )

            val result = authRepository.login(
                email = emailLimpo,
                password = passwordLimpa
            )

            if (result.isSuccess) {
                uiState.value = uiState.value.copy(
                    loading = false,
                    error = null
                )

                onSuccess()
            } else {
                uiState.value = uiState.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message ?: "Login falhou."
                )
            }
        }
    }
}