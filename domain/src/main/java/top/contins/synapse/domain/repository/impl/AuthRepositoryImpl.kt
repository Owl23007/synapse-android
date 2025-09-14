package top.contins.synapse.domain.repository.impl

import android.util.Log
import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.User
import top.contins.synapse.domain.model.CaptchaResponse
import top.contins.synapse.domain.repository.AuthRepository
import top.contins.synapse.network.api.ApiService
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    override suspend fun login(identifier: String, password: String, serverEndpoint: String): AuthResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        serverEndpoint: String
    ): AuthResult<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getCaptcha(serverEndpoint: String): CaptchaResponse {
        Log.d("Auth", "Fetching captcha from $serverEndpoint/auth/captcha")
        val result = apiService.getCaptcha("$serverEndpoint/auth/captcha") // Result<String>

        Log.d("Auth", "Captcha response: ${result.data}")
        // 检查是否成功
        if (result.code != 0) {
            throw Exception("请求失败: ${result.message}")
        }

        // 获取 data，如果为 null 则抛异常
        val responseString = result.data ?: throw Exception("验证码数据为空")

        // 解析格式
        val parts = responseString.split(":", limit = 2)
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid captcha response format: $responseString")
        }

        return CaptchaResponse(
            captchaId = parts[0].trim(),
            captchaImageBase64 = parts[1].trim()
        )
    }
    override suspend fun afterLogin(serverEndpoint: String, email: String, password: String): AuthResult<User> {
        TODO("Not yet implemented")
    }
}