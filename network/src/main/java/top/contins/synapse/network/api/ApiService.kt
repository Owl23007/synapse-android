package top.contins.synapse.network.api

import retrofit2.http.GET
import retrofit2.http.Url
import top.contins.synapse.network.model.Result

interface ApiService {
    @GET
    suspend fun getCaptcha(@Url fullUrl: String): Result<String>
}