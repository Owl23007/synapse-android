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
            
            Log.d("TokenValidation", "Validating refresh token for server: $serverEndpoint")
            
            // 1. 验证refresh token是否有效
            val isRefreshTokenValid = validateRefreshToken(serverEndpoint, refreshToken)
            
            if (!isRefreshTokenValid) {
                Log.d("TokenValidation", "Refresh token invalid, clearing tokens")
                tokenManager.clearTokens()
                return TokenValidationResult.Invalid
            }
            
            Log.d("TokenValidation", "Refresh token valid, refreshing access token")
            
            // 2. refresh token有效，刷新access token
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
     * 验证refresh token是否有效
     */
    private suspend fun validateRefreshToken(serverEndpoint: String, refreshToken: String): Boolean {
        return try {
            val result = apiService.validateToken("$serverEndpoint/auth/validate", refreshToken)
            result.code == 0 && result.data != null
        } catch (e: Exception) {
            Log.e("TokenValidation", "Error validating refresh token", e)
            false
        }
    }
    
    /**
     * 使用refresh token刷新access token
     */
    private suspend fun refreshAccessToken(serverEndpoint: String, refreshToken: String): Boolean {
        return try {
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