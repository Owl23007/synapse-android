package top.contins.synapse.data.repository

import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.domain.repository.TokenRepository
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager
) : TokenRepository {
    override fun saveTokens(accessToken: String, refreshToken: String, serverEndpoint: String?) {
        tokenManager.saveTokens(accessToken, refreshToken, serverEndpoint)
    }

    override fun getAccessToken(): String? = tokenManager.getAccessToken()

    override fun getRefreshToken(): String? = tokenManager.getRefreshToken()

    override fun getServerEndpoint(): String? = tokenManager.getServerEndpoint()

    override fun clearTokens() = tokenManager.clearTokens()

    override fun hasValidTokens(): Boolean = tokenManager.hasValidTokens()
}
