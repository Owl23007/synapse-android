package top.contins.synapse.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import top.contins.synapse.network.model.ChatRequest
import top.contins.synapse.network.model.LoginRequest
import top.contins.synapse.network.model.ModelInfo
import top.contins.synapse.network.model.RegisterRequest
import top.contins.synapse.network.model.Result
import top.contins.synapse.network.model.ServiceRoutesResponse
import top.contins.synapse.network.model.TaskResponse
import top.contins.synapse.network.model.TokenResponse
import top.contins.synapse.network.model.UserSelfProfileResponse

interface ApiService {
    @GET("auth/captcha")
    suspend fun getCaptcha(): Result<String>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Result<TokenResponse>

    @GET("profile/me")
    suspend fun getUserProfile(): Result<UserSelfProfileResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Refresh-Token") refreshToken: String): Result<TokenResponse>

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Result<String>

    @GET("service-registry/routes")
    suspend fun getServiceRoutes(): ServiceRoutesResponse

    // AI聊天相关API
    @GET("ai/models")
    suspend fun getSupportedModels(): Result<List<ModelInfo>>

    @POST("synapse/chat")
    suspend fun startChat(@Body chatRequest: ChatRequest): Result<TaskResponse>

    @GET("synapse/chat/{taskId}")
    @Streaming
    suspend fun streamChat(@Path("taskId") taskId: String): Response<ResponseBody>

    @DELETE("synapse/chat/{taskId}")
    suspend fun stopChat(@Path("taskId") taskId: String): Result<String>

    @POST("synapse/chat/{taskId}/continue")
    suspend fun continueChat(@Path("taskId") taskId: String, @Body chatRequest: ChatRequest): Result<TaskResponse>
}