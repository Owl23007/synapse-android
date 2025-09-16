package top.contins.synapse.domain.usecase

import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.User
import top.contins.synapse.domain.model.CaptchaResponse
import top.contins.synapse.domain.repository.AuthRepository
import javax.inject.Inject


class AuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend fun login(email: String, password: String, serverEndpoint: String): AuthResult<User> {
        return repository.login(email, password, serverEndpoint)
    }

    suspend fun register(
        email: String,
        username: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        serverEndpoint: String
    ): AuthResult<String?> {
        return repository.register(email, username, password, captchaId, captchaCode, serverEndpoint)
    }

    suspend fun getCaptcha(serverEndpoint: String): AuthResult<CaptchaResponse> {
        return try {
            val response = repository.getCaptcha(serverEndpoint)
            AuthResult.Success(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "发生未知错误")
        }
    }
}