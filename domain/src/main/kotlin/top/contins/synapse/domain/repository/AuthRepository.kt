package top.contins.synapse.domain.repository

import top.contins.synapse.domain.model.auth.AuthResult
import top.contins.synapse.domain.model.auth.User
import top.contins.synapse.domain.model.auth.CaptchaResponse

interface AuthRepository {
    suspend fun login(identifier: String, password: String, serverEndpoint: String): AuthResult<User>
    suspend fun register(
        email: String,
        username: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        serverEndpoint: String
    ): AuthResult<String?>
    suspend fun getCaptcha(serverEndpoint: String): CaptchaResponse
    suspend fun checkAuth(): AuthResult<User>
}
