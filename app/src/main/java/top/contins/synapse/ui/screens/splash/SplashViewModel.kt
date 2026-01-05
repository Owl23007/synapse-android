package top.contins.synapse.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.domain.usecase.ValidateTokenOnStartupUseCase
import top.contins.synapse.domain.model.TokenValidationResult
import javax.inject.Inject

/**
 * 启动页 UI 状态
 * 
 * 表示启动屏幕在不同阶段的状态
 */
sealed class SplashUiState {
    /** 初始加载中状态，验证token并获取路由信息 */
    object Loading : SplashUiState()
    
    /** Token 无效或不存在，需要导航到认证页面 */
    object NavigateToAuth : SplashUiState()
    
    /** Token 有效，导航到主页面 */
    object NavigateToMain : SplashUiState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateTokenOnStartupUseCase: ValidateTokenOnStartupUseCase,
    private val tokenManager: TokenManager
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
                
                when (result) {
                    TokenValidationResult.NoTokens -> {
                        // 没有保存的tokens，需要登录
                        _uiState.value = SplashUiState.NavigateToAuth
                    }
                    TokenValidationResult.Refreshed -> {
                        // 通过refresh token刷新成功，加载路由信息
                        _uiState.value = SplashUiState.NavigateToMain
                    }
                    TokenValidationResult.Invalid -> {
                        // tokens无效，需要重新登录
                        _uiState.value = SplashUiState.NavigateToAuth
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
