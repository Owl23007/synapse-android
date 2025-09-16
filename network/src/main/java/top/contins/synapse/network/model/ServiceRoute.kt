package top.contins.synapse.network.model

import com.google.gson.annotations.SerializedName

/**
 * 服务路由信息数据类
 */
data class ServiceRoute(
    @SerializedName("serviceName")
    val serviceName: String,
    @SerializedName("baseUrl")
    val baseUrl: String,
    @SerializedName("pathPrefix")
    val pathPrefix: String
) {
    /**
     * 获取完整的服务端点URL
     */
    fun getFullEndpoint(): String {
        return "$baseUrl$pathPrefix"
    }
}

/**
 * 服务注册中心返回的路由响应
 */
data class ServiceRoutesResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Map<String, ServiceRoute>?
)