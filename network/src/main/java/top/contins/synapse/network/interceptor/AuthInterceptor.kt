package top.contins.synapse.network.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import top.contins.synapse.network.api.ApiService
import top.contins.synapse.network.api.TokenProvider
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val refreshApiServiceProvider: () -> ApiService
) : Interceptor {

    private val isRefreshing = AtomicBoolean(false)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 添加access token到请求头
        val accessToken = tokenProvider.getAccessToken()
        val requestWithToken = if (!accessToken.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(requestWithToken)

        // 如果返回401且有refresh token，尝试刷新token
        if (response.code == 401 && !tokenProvider.getRefreshToken().isNullOrEmpty()) {
            response.close() // 关闭原始响应

            synchronized(this) {
                // 双重检查锁定模式，避免多个线程同时刷新token
                val currentToken = tokenProvider.getAccessToken()
                if (currentToken == accessToken && isRefreshing.compareAndSet(false, true)) {
                    try {
                        if (refreshTokenSync()) {
                            // 刷新成功，用新token重新请求
                            val newAccessToken = tokenProvider.getAccessToken()
                            if (!newAccessToken.isNullOrEmpty()) {
                                val newRequest = originalRequest.newBuilder()
                                    .addHeader("Authorization", "Bearer $newAccessToken")
                                    .build()
                                return chain.proceed(newRequest)
                            }
                        } else {
                            // 刷新失败，清除tokens
                            tokenProvider.clearTokens()
                        }
                    } finally {
                        isRefreshing.set(false)
                    }
                } else if (isRefreshing.get()) {
                    // 等待其他线程完成刷新
                    waitForRefresh()

                    // 检查是否有新的token
                    val newAccessToken = tokenProvider.getAccessToken()
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
            val refreshToken = tokenProvider.getRefreshToken()
            val serverEndpoint = tokenProvider.getServerEndpoint()

            if (refreshToken.isNullOrEmpty() || serverEndpoint.isNullOrEmpty()) {
                return false
            }

            val refreshApiService = refreshApiServiceProvider()

            // 使用保存的服务器端点
            val result = runBlocking {
                try {
                    refreshApiService.refreshToken(refreshToken)
                } catch (_: Exception) {
                    null
                }
            }

            if (result?.code == 0 && result.data != null) {
                val tokenResponse = result.data!!
                // 保存新的tokens，保持原有的服务器地址
                tokenProvider.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken, serverEndpoint)
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
        //等待
        var attempts = 0
        while (isRefreshing.get() && attempts < 50) { // 最多等待5秒
            try {
                Thread.sleep(100)
                attempts++
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }
}