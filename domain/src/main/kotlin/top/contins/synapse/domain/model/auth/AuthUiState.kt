package top.contins.synapse.domain.model.auth

import top.contins.synapse.domain.model.auth.CaptchaResponse
import top.contins.synapse.domain.model.auth.User

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class LoginSuccess(val user: User, val isAutoLogin: Boolean = false) : AuthUiState()
    data class LoginError(val message: String) : AuthUiState()
    data class RegisterSuccess(val message: String) : AuthUiState()
    data class RegisterError(val message: String) : AuthUiState()
    data class CaptchaLoaded(val response: CaptchaResponse) : AuthUiState()
    data class CaptchaError(val message: String) : AuthUiState()
    data class AfterLoginError(val message: String) : AuthUiState()
}

val AuthUiState.errorMessage: String?
    get() = when (this) {
        is AuthUiState.LoginError -> message
        is AuthUiState.RegisterError -> message
        is AuthUiState.CaptchaError -> message
        is AuthUiState.AfterLoginError -> message
        else -> null  // 处理所有非错误状态
    }