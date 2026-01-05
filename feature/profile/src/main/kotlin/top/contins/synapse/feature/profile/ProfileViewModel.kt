package top.contins.synapse.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.auth.AuthResult
import top.contins.synapse.domain.model.auth.User
import top.contins.synapse.domain.usecase.auth.AuthUseCase
import top.contins.synapse.domain.usecase.auth.LogoutUseCase
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object LoggingOut : ProfileUiState()
    object LoggedOut : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = authUseCase.checkAuth()) {
                is AuthResult.Success -> {
                    _uiState.value = ProfileUiState.Success(result.data)
                }
                is AuthResult.Error -> {
                    _uiState.value = ProfileUiState.Error(result.message)
                }
            }
        }
    }

    /**
     * 执行登出操作
     */
    fun logout() {
        viewModelScope.launch {
            try {
                _uiState.value = ProfileUiState.LoggingOut
                
                // 执行登出逻辑
                logoutUseCase()
                
                // 通知UI跳转到登录页
                _uiState.value = ProfileUiState.LoggedOut
            } catch (e: Exception) {
                // 即使出错也应该清理本地数据并跳转到登录页
                _uiState.value = ProfileUiState.LoggedOut
            }
        }
    }

    /**
     * 重置状态（当已经处理完登出后调用）
     */
    fun resetState() {
        _uiState.value = ProfileUiState.Loading
    }
}