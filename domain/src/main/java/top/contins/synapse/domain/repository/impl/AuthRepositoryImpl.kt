package top.contins.synapse.domain.repository.impl

import android.net.http.HttpException
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresExtension
import top.contins.synapse.data.storage.TokenManager
import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.User
import top.contins.synapse.domain.model.CaptchaResponse
import top.contins.synapse.domain.repository.AuthRepository
import top.contins.synapse.network.api.ApiService
import top.contins.synapse.network.model.LoginRequest
import top.contins.synapse.network.model.RegisterRequest
import java.io.IOException
import java.security.NoSuchAlgorithmException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.spec.InvalidKeySpecException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun login(identifier: String, password: String, serverEndpoint: String): AuthResult<User> {
        Log.d("Auth", "Logging in user: $identifier")

        val request = LoginRequest(identifier, password)

        return try {
            val result = apiService.login("$serverEndpoint/auth/login", request)

            if (result.code == 0 && result.data != null) {
                val tokenResponse = result.data!!
                
                // 保存tokens和服务器地址
                tokenManager.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken, serverEndpoint)
                
                // 解析JWT token获取用户信息 (这里简化处理)
                val user = parseUserFromToken(tokenResponse.accessToken) ?: run {
                    return AuthResult.Error("无法解析用户信息")
                }
                
                AuthResult.Success(user)
            } else {
                AuthResult.Error("登录失败: ${result.message ?: "未知错误"}")
            }
        } catch(e: HttpException) {
            return AuthResult.Error("登录失败: ${e.message}")
        } catch (e: IOException) {
            Log.e("Auth", "网络错误", e)
            AuthResult.Error("网络连接失败，请检查网络设置")
        } catch (e: Exception) {
            Log.e("Auth", "登录异常", e)
            AuthResult.Error("登录失败: ${e.message ?: "未知错误"}")
        }
    }

    private fun parseUserFromToken(accessToken: String): User? {
        return try {
            // 这里应该解析JWT token获取用户信息
            // 简化处理，返回一个默认用户对象
            // 在实际项目中，你需要解析JWT的payload部分
            val parts = accessToken.split(".")
            if (parts.size >= 2) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                // 这里应该解析JSON获取用户信息
                // 现在返回一个占位符用户
                User(
                    id = 1L,
                    email = "user@example.com",
                    username = "username",
                    nickname = "nickname",
                    avatar = "",
                    background = "",
                    signature = "",
                    serverEndpoint = ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("Auth", "解析token失败", e)
            null
        }
    }
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7) // 兼容 Android 7.0 以上版本
    override suspend fun register(
        email: String,
        username: String,
        password: String,
        captchaId: String,
        captchaCode: String,
        serverEndpoint: String
    ): AuthResult<String?> {
        Log.d("Auth", "Registering user: $username")

        val request = RegisterRequest(username, email, password, captchaId, captchaCode)

        return try {
            val result = apiService.register("$serverEndpoint/registration/register", request)

            if (result.code == 0 && result.data != null) {
                AuthResult.Success(result.data)
            } else {
                AuthResult.Error("注册失败: ${result.message ?: "未知错误"}")
            }
        } catch(e: HttpException) {
            return  AuthResult.Error("注册失败: ${e.message}")
        }
        catch (e: IOException) {
            Log.e("Auth", "网络错误", e)
            return AuthResult.Error("网络连接失败，请检查网络")
        } catch (e: Exception) {
            Log.e("Auth", "未知错误", e)
            return AuthResult.Error("注册失败，请重试")
        }
    }

    override suspend fun getCaptcha(serverEndpoint: String): CaptchaResponse {
        Log.d("Auth", "Fetching captcha from $serverEndpoint/auth/captcha")
        val result = apiService.getCaptcha("$serverEndpoint/auth/captcha")

        Log.d("Auth", "Captcha response: ${result.data}")
        if (result.code != 0) {
            throw Exception("请求失败: ${result.message}")
        }

        val responseString = result.data ?: throw Exception("验证码数据为空")

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

    // ✅ 修复：使用 android.util.Base64 替代 java.util.Base64（兼容所有 Android 版本）
    private fun encryptPassword(password: String, username: String): String {
        val salt = username.toByteArray()
        val iterations = 10000
        val keyLength = 256

        return try {
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP) // ✅ Android 安全编码，无换行
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("加密算法不支持", e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("密钥规范无效", e)
        }
    }
}