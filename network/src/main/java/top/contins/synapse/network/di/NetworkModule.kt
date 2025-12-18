package top.contins.synapse.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

    @Provides
    @Singleton
    @Named("noAuth")
    fun provideNoAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenProvider: TokenProvider,
        @Named("refreshApi") refreshApiServiceProvider: Provider<ApiService>
    ): AuthInterceptor {
        return AuthInterceptor(tokenProvider, refreshApiServiceProvider)
    }

    @Provides
    @Singleton
    @Named("refreshApi")
    fun provideRefreshApiService(@Named("noAuth") okHttpClient: OkHttpClient): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2/")
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
            .readTimeout(0, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(null)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
