package top.contins.synapse.domain.model.auth

import top.contins.synapse.domain.model.auth.CaptchaResponse

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

fun AuthResult<CaptchaResponse>.toCaptchaUiState(): AuthUiState =
    when (this) {
        is AuthResult.Success -> AuthUiState.CaptchaLoaded(data)
        is AuthResult.Error -> AuthUiState.CaptchaError(message)
    }