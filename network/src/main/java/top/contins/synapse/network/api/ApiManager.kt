package top.contins.synapse.network.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import top.contins.synapse.network.interceptor.AuthInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiManager @Inject constructor(
    private val tokenProvider: TokenProvider
) {
    private var currentBaseUrl: String? = null
    private var apiService: ApiService? = null
    private var okHttpClient: OkHttpClient? = null

    fun initializeWithBaseUrl(baseUrl: String) {
        // 确保 URL 以 / 结尾（Retrofit 要求）
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        currentBaseUrl = normalizedUrl
        okHttpClient = createOkHttpClient()
        apiService = createApiService(currentBaseUrl!!, okHttpClient!!)
    }

    fun getApiService(): ApiService {
        return apiService ?: throw IllegalStateException("ApiManager 未初始化,请先调用 initializeWithBaseUrl 方法初始化")
    }

    fun getCurrentBaseUrl(): String? {
        return currentBaseUrl
    }

    fun isInitialized(): Boolean {
        return apiService != null && currentBaseUrl != null
    }

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        // 创建一个新的AuthInterceptor实例，使用当前的TokenProvider
        val authInterceptor = AuthInterceptor(tokenProvider) {
            // 创建用于刷新token的API服务实例
            val refreshOkHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .retryOnConnectionFailure(false)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl ?: "http://placeholder/") // 使用当前基础URL或占位符
                .client(refreshOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(ApiService::class.java)
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .readTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
    }

    private fun createApiService(baseUrl: String, httpClient: OkHttpClient): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }
}