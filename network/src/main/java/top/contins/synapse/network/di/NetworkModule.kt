package top.contins.synapse.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import top.contins.synapse.network.api.ApiService
import top.contins.synapse.network.api.TokenProvider
import top.contins.synapse.network.interceptor.AuthInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun getUserAgent(): String {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }

    private val browserHeadersInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            // 1. User-Agent
            .header("User-Agent", getUserAgent())
            // 2. Accept（告知服务器能接收的内容类型）
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            // 3. Accept-Language（语言偏好）
            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
            // 4. Accept-Encoding（支持的压缩格式）
            .header("Accept-Encoding", "gzip, deflate, br")
            // 5. Sec-Fetch-*
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-User", "?1")
            // 6. 常见浏览器附加头（可选但推荐）
            .header("DNT", "1") // Do Not Track
            .header("Upgrade-Insecure-Requests", "1") // 告知服务器可升级到 HTTPS
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideNoAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(browserHeadersInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider,
        @Named("refreshApi") refreshApiServiceProvider: Provider<ApiService>
    ): AuthInterceptor {
        return AuthInterceptor(tokenProvider) {
            refreshApiServiceProvider.get()
        }
    }

    @Provides
    @Singleton
    @Named("refreshApi")
    fun provideRefreshApiService(@Named("noAuth") okHttpClient: OkHttpClient): ApiService {
        // 使用占位符URL，实际URL将在运行时通过@Url注解指定
        val retrofit = Retrofit.Builder()
            .baseUrl("http://placeholder/") // 占位符URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS)

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(browserHeadersInterceptor)
            .readTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(null)
            .retryOnConnectionFailure(false)
            .build()
    }
}