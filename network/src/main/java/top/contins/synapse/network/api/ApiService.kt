package top.contins.synapse.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url
import top.contins.synapse.network.model.ChatModels
import top.contins.synapse.network.model.ChatRequest
import top.contins.synapse.network.model.LoginRequest
import top.contins.synapse.network.model.ModelInfo
import top.contins.synapse.network.model.RegisterRequest
import top.contins.synapse.network.model.Result
import top.contins.synapse.network.model.ServiceRoutesResponse
import top.contins.synapse.network.model.TaskResponse
import top.contins.synapse.network.model.TokenResponse

interface ApiService {
    @GET
    suspend fun getCaptcha(@Url fullUrl: String): Result<String>

    @POST
    suspend fun login(@Url fullUrl: String, @Body loginRequest: LoginRequest): Result<TokenResponse>

    @FormUrlEncoded
    @POST
    suspend fun validateToken(@Url fullUrl: String, @Field("token") token: String): Result<String>

    @FormUrlEncoded  
    @POST
    suspend fun refreshToken(@Url fullUrl: String, @Field("refreshToken") refreshToken: String): Result<TokenResponse>

    @POST
    suspend fun register(@Url fullUrl: String, @Body registerRequest: RegisterRequest): Result<String>

    @GET
    suspend fun getServiceRoutes(@Url fullUrl: String): ServiceRoutesResponse

    // AI聊天相关API
    @GET
    suspend fun getSupportedModels(@Url fullUrl: String): Result<List<ModelInfo>>

    @POST
    suspend fun startChat(@Url fullUrl: String, @Body chatRequest: ChatRequest): Result<TaskResponse>

    @GET
    @Streaming
    suspend fun streamChat(@Url fullUrl: String): Response<ResponseBody>

    @DELETE
    suspend fun stopChat(@Url fullUrl: String): Result<String>
}