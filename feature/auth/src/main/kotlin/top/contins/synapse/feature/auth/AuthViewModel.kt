package top.contins.synapse.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.AuthUiState
import top.contins.synapse.domain.usecase.AuthUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authUseCase.checkAuth()) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.LoginSuccess(result.data, isAutoLogin = true)
                }
                is AuthResult.Error -> {
                    // 如果检查失败（未登录或token无效），显示登录界面
                    _uiState.value = AuthUiState.Idle
                }
            }
        }
    }

    fun login(email: String, password: String, serverEndpoint: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authUseCase.login(email, password, serverEndpoint)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.LoginSuccess(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.LoginError(result.message)
                }
            }
        }
    }

    fun register(
        email: String,
        username: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        serverEndpoint: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authUseCase.register(
                email, username, password, captchaId, captchaCode, serverEndpoint
            )) {
                is AuthResult.Success -> {
                    result.data?.let { _uiState.value = AuthUiState.RegisterSuccess(it) }
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.RegisterError(result.message)
                }
            }
        }
    }

    fun getCaptcha(serverEndpoint: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            when (val result = authUseCase.getCaptcha(serverEndpoint)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.CaptchaLoaded(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.CaptchaError(result.message)
                }
            }
        }
    }

    fun resetAuthState() {
        _uiState.value = AuthUiState.Idle
    }
}