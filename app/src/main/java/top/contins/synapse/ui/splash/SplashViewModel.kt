package top.contins.synapse.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.usecase.auth.ValidateTokenOnStartupUseCase
import top.contins.synapse.domain.model.auth.TokenValidationResult
import top.contins.synapse.feature.schedule.utils.LunarHelper
import top.contins.synapse.network.utils.BingImageHelper
import java.time.LocalDate
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
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        preloadData()
        validateTokens()
    }

    private fun preloadData() {
        viewModelScope.launch {
            // Preload 2 years of Lunar Calendar
            val currentYear = LocalDate.now().year
            launch { LunarHelper.preloadLunarYear(currentYear) }
            launch { LunarHelper.preloadLunarYear(currentYear + 1) }

            // Preload Bing Image
            launch {
                try {
                    BingImageHelper.getTodayImageUrl(context)
                } catch (_: Exception) {
                    // Ignore network errors
                }
            }
        }
    }
    
    private fun validateTokens() {
        viewModelScope.launch {
            try {
                val result = validateTokenOnStartupUseCase()
                // 等待 3 秒以展示启动屏幕
                delay(1000)
                
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
            } catch (_: Exception) {
                // 如果验证过程中出现错误，导航到登录页面
                _uiState.value = SplashUiState.NavigateToAuth
            }
        }
    }


}
