package top.contins.synapse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.usecase.ValidateTokenOnStartupUseCase
import top.contins.synapse.network.service.TokenValidationResult
import javax.inject.Inject

sealed class SplashUiState {
    object Loading : SplashUiState()
    object NavigateToAuth : SplashUiState()
    object NavigateToMain : SplashUiState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateTokenOnStartupUseCase: ValidateTokenOnStartupUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        validateTokens()
    }
    
    private fun validateTokens() {
        viewModelScope.launch {
            try {
                val result = validateTokenOnStartupUseCase()
                
                _uiState.value = when (result) {
                    TokenValidationResult.NoTokens -> {
                        // 没有保存的tokens，需要登录
                        SplashUiState.NavigateToAuth
                    }
                    TokenValidationResult.Refreshed -> {
                        // 通过refresh token刷新成功，进入主界面
                        SplashUiState.NavigateToMain
                    }
                    TokenValidationResult.Invalid -> {
                        // tokens无效，需要重新登录
                        SplashUiState.NavigateToAuth
                    }
                }
            } catch (e: Exception) {
                // 如果验证过程中出现错误，导航到登录页面
                _uiState.value = SplashUiState.NavigateToAuth
            }
        }
    }
    
    fun resetState() {
        _uiState.value = SplashUiState.Loading
    }
}