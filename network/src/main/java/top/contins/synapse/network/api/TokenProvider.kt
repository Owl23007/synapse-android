package top.contins.synapse.network.api

/**
 * Token提供者接口
 * Network模块通过此接口获取Token，而不依赖具体的存储实现
 */
interface TokenProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getServerEndpoint(): String?
    fun saveTokens(accessToken: String, refreshToken: String, serverEndpoint: String? = null)
    fun clearTokens()
}
