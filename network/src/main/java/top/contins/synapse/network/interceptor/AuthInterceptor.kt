package top.contins.synapse.network.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.network.api.ApiService
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    @field:Named("refreshApi") private val refreshApiService: ApiService
) : Interceptor {
    
    private val isRefreshing = AtomicBoolean(false)
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 添加access token到请求头
        val accessToken = tokenManager.getAccessToken()
        val requestWithToken = if (!accessToken.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }
        
        val response = chain.proceed(requestWithToken)
        
        // 如果返回401且有refresh token，尝试刷新token
        if (response.code == 401 && !tokenManager.getRefreshToken().isNullOrEmpty()) {
            response.close() // 关闭原始响应
            
            synchronized(this) {
                // 双重检查锁定模式，避免多个线程同时刷新token
                val currentToken = tokenManager.getAccessToken()
                if (currentToken == accessToken && isRefreshing.compareAndSet(false, true)) {
                    try {
                        if (refreshTokenSync()) {
                            // 刷新成功，用新token重新请求
                            val newAccessToken = tokenManager.getAccessToken()
                            if (!newAccessToken.isNullOrEmpty()) {
                                val newRequest = originalRequest.newBuilder()
                                    .addHeader("Authorization", "Bearer $newAccessToken")
                                    .build()
                                return chain.proceed(newRequest)
                            }
                        } else {
                            // 刷新失败，清除tokens
                            tokenManager.clearTokens()
                        }
                    } finally {
                        isRefreshing.set(false)
                    }
                } else if (isRefreshing.get()) {
                    // 等待其他线程完成刷新
                    waitForRefresh()
                    
                    // 检查是否有新的token
                    val newAccessToken = tokenManager.getAccessToken()
                    if (!newAccessToken.isNullOrEmpty() && newAccessToken != currentToken) {
                        val newRequest = originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $newAccessToken")
                            .build()
                        return chain.proceed(newRequest)
                    }
                }
            }
        }
        
        return response
    }
    
    private fun refreshTokenSync(): Boolean {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            val serverEndpoint = tokenManager.getServerEndpoint()
            
            if (refreshToken.isNullOrEmpty() || serverEndpoint.isNullOrEmpty()) {
                return false
            }
            
            // 使用保存的服务器端点而不是从URL中提取
            val result = runBlocking {
                try {
                    refreshApiService.refreshToken("$serverEndpoint/auth/refresh", refreshToken)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (result?.code == 0 && result.data != null) {
                val tokenResponse = result.data!!
                // 保存新的tokens，保持原有的服务器地址
                tokenManager.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken, serverEndpoint)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun waitForRefresh() {
        // 简单的等待机制
        var attempts = 0
        while (isRefreshing.get() && attempts < 50) { // 最多等待5秒
            try {
                Thread.sleep(100)
                attempts++
            } catch (e: InterruptedException) {

                Thread.currentThread().interrupt()
                break
            }
        }
    }
}