package top.contins.synapse.domain.usecase.auth

import top.contins.synapse.domain.model.auth.AuthResult
import top.contins.synapse.domain.model.auth.CaptchaResponse
import top.contins.synapse.domain.model.auth.User
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
            AuthResult.Error("获取验证码失败，请检查网络或重试")
        }
    }

    suspend fun checkAuth(): AuthResult<User> {
        return repository.checkAuth()
    }
}