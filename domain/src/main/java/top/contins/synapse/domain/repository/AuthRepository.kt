package top.contins.synapse.domain.repository

import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.User
import top.contins.synapse.domain.model.CaptchaResponse

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
    suspend fun afterLogin(serverEndpoint: String, email: String, password: String): AuthResult<User>
    suspend fun checkAuth(): AuthResult<User>
}
