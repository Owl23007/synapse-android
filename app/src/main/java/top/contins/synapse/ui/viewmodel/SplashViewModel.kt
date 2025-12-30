package top.contins.synapse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.domain.service.RouteManager
import top.contins.synapse.domain.usecase.ValidateTokenOnStartupUseCase
import top.contins.synapse.domain.model.TokenValidationResult
import javax.inject.Inject

/**
 * 启动页UI状态封装
 */
sealed class SplashUiState {
    object Loading : SplashUiState()        // 加载中状态
    object NavigateToAuth : SplashUiState() // 导航到认证页面
    object NavigateToMain : SplashUiState() // 导航到主页面
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val validateTokenOnStartupUseCase: ValidateTokenOnStartupUseCase,
    private val routeManager: RouteManager,
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
                        loadRoutesAndNavigate()
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
    
    /**
     * 加载路由信息并导航到主界面
     */
    private suspend fun loadRoutesAndNavigate() {
        try {
            // 获取服务器端点用于加载路由
            val serverEndpoint = tokenManager.getServerEndpoint()
            
            if (!serverEndpoint.isNullOrEmpty()) {
                // 尝试加载路由信息
                val routeLoadSuccess = routeManager.loadRoutes(serverEndpoint)
                
                if (routeLoadSuccess) {
                    // 路由加载成功，进入主界面
                    _uiState.value = SplashUiState.NavigateToMain
                } else {
                    // 路由加载失败，但token是有效的，仍然进入主界面
                    _uiState.value = SplashUiState.NavigateToMain
                }
            } else {
                // 没有服务器端点信息，直接进入主界面
                _uiState.value = SplashUiState.NavigateToMain
            }
        } catch (e: Exception) {
            // 路由加载出错，但仍然进入主界面
            _uiState.value = SplashUiState.NavigateToMain
        }
    }
    
    fun resetState() {
        _uiState.value = SplashUiState.Loading
    }
}