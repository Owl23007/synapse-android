package top.contins.synapse.network.service

import android.util.Log
import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.network.api.ApiService
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenValidationService @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("refreshApi") private val apiService: ApiService
) {
    
    /**
     * 启动时验证token的完整流程
     * 1. 检查是否有保存的refresh token和服务器地址
     * 2. 验证refresh token是否有效
     * 3. 如果有效，刷新access token
     * 4. 如果无效，清除所有tokens
     */
    suspend fun validateTokenOnStartup(): TokenValidationResult {
        try {
            // 检查是否有必要的信息
            val refreshToken = tokenManager.getRefreshToken()
            val serverEndpoint = tokenManager.getServerEndpoint()
            
            if (refreshToken.isNullOrEmpty() || serverEndpoint.isNullOrEmpty()) {
                Log.d("TokenValidation", "Missing refresh token or server endpoint")
                return TokenValidationResult.NoTokens
            }
            
            Log.d("TokenValidation", "Validating token for server: $serverEndpoint")

            // 尝试使用 Refresh Token 刷新 Access Token
            // 因为没有单独的 validate 接口，我们直接尝试刷新来验证有效性
            Log.d("TokenValidation", "Trying to refresh token")
            val refreshSuccess = refreshAccessToken(serverEndpoint, refreshToken)
            
            if (refreshSuccess) {
                Log.d("TokenValidation", "Token refresh successful")
                return TokenValidationResult.Refreshed
            } else {
                Log.d("TokenValidation", "Token refresh failed, clearing tokens")
                tokenManager.clearTokens()
                return TokenValidationResult.Invalid
            }
            
        } catch (e: Exception) {
            Log.e("TokenValidation", "Error during token validation", e)
            tokenManager.clearTokens()
            return TokenValidationResult.Invalid
        }
    }
    
    /**
     * 使用refresh token刷新access token
     */
    private suspend fun refreshAccessToken(serverEndpoint: String, refreshToken: String): Boolean {
        return try {
            // 添加 Bearer 前缀，以符合后端兼容性 (虽然后端说 required=false 且兼容 Bearer，但为了保险)
            // 后端代码: if (refreshToken.startsWith("Bearer ")) ...
            // 这里直接传原始值，或者加 Bearer 都可以。后端代码显示它会处理 Bearer。
            // 既然是 Header 传递，通常不需要 Bearer 前缀，除非是 Authorization header。
            // 但后端代码 specifically checks for "Bearer ". Let's just pass it as is, assuming stored token is raw.
            // Wait, stored token usually doesn't have Bearer.
            // Let's pass "Bearer $refreshToken" just to be safe if backend expects it or handles it.
            // Actually backend says: if (refreshToken.startsWith("Bearer ")) { refreshToken = refreshToken.substring(7).trim(); }
            // So it supports both. I will pass raw token.
            
            val result = apiService.refreshToken("$serverEndpoint/auth/refresh", refreshToken)
            
            if (result.code == 0 && result.data != null) {
                val tokenResponse = result.data!!
                // 保存新的tokens，保持原有的服务器地址
                tokenManager.saveTokens(
                    tokenResponse.accessToken, 
                    tokenResponse.refreshToken, 
                    serverEndpoint
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("TokenValidation", "Error refreshing token", e)
            false
        }
    }
}

sealed class TokenValidationResult {
    object NoTokens : TokenValidationResult()  // 没有保存的tokens
    object Refreshed : TokenValidationResult() // 通过refresh token刷新成功
    object Invalid : TokenValidationResult()   // tokens无效，需要重新登录
}