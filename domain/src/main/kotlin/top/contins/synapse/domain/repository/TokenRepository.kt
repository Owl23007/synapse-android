package top.contins.synapse.domain.repository

interface TokenRepository {
    fun saveTokens(accessToken: String, refreshToken: String, serverEndpoint: String? = null)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getServerEndpoint(): String?
    fun clearTokens()
    fun hasValidTokens(): Boolean
}
