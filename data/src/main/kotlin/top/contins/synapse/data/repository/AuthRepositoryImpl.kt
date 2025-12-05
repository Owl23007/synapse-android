package top.contins.synapse.data.repository

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
                
                tokenManager.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken, serverEndpoint)
                
                try {
                    val profileResult = apiService.getUserProfile("$serverEndpoint/profile/me")
                    if (profileResult.code == 0 && profileResult.data != null) {
                        val user = mapProfileToUser(profileResult.data!!, serverEndpoint)
                        AuthResult.Success(user)
                    } else {
                        Log.w("Auth", "Failed to fetch user profile: ${profileResult.message}")
                        val user = User(
                            id = 0L,
                            email = identifier,
                            username = identifier,
                            nickname = identifier,
                            serverEndpoint = serverEndpoint
                        )
                        AuthResult.Success(user)
                    }
                } catch (e: Exception) {
                    Log.e("Auth", "Error fetching user profile", e)
                    val user = User(
                        id = 0L,
                        email = identifier,
                        username = identifier,
                        nickname = identifier,
                        serverEndpoint = serverEndpoint
                    )
                    AuthResult.Success(user)
                }
            } else {
                AuthResult.Error(result.message ?: "登录失败")
            }
        } catch(e: HttpException) {
            return AuthResult.Error("登录失败，服务器响应错误")
        } catch (e: IOException) {
            Log.e("Auth", "Network error during login", e)
            AuthResult.Error("网络连接失败，请检查网络设置")
        } catch (e: Exception) {
            Log.e("Auth", "Login exception", e)
            AuthResult.Error("登录失败，请稍后重试")
        }
    }

    override suspend fun checkAuth(): AuthResult<User> {
        val accessToken = tokenManager.getAccessToken()
        val serverEndpoint = tokenManager.getServerEndpoint()

        if (accessToken.isNullOrEmpty() || serverEndpoint.isNullOrEmpty()) {
            return AuthResult.Error("未登录")
        }

        return try {
            val profileResult = apiService.getUserProfile("$serverEndpoint/profile/me")
            
            if (profileResult.code == 0 && profileResult.data != null) {
                val user = mapProfileToUser(profileResult.data!!, serverEndpoint)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Token无效或过期")
            }
        } catch (e: Exception) {
            Log.e("Auth", "Check auth failed", e)
            AuthResult.Error("验证失败，请重新登录")
        }
    }

    private fun mapProfileToUser(profile: top.contins.synapse.network.model.UserSelfProfileResponse, serverEndpoint: String): User {
        return User(
            id = profile.userId,
            email = profile.email ?: "",
            username = profile.username,
            nickname = profile.nickname ?: profile.username,
            avatar = profile.avatarImage ?: "",
            background = profile.backgroundImage ?: "",
            signature = profile.signature ?: "",
            serverEndpoint = serverEndpoint
        )
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
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
                AuthResult.Error(result.message ?: "注册失败")
            }
        } catch(e: HttpException) {
            return AuthResult.Error("注册失败，服务器响应错误")
        } catch (e: IOException) {
            Log.e("Auth", "Network error during registration", e)
            return AuthResult.Error("网络连接失败，请检查网络")
        } catch (e: Exception) {
            Log.e("Auth", "Registration exception", e)
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

    private fun encryptPassword(password: String, username: String): String {
        val salt = username.toByteArray()
        val iterations = 10000
        val keyLength = 256

        return try {
            val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("加密算法不支持", e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("密钥规范无效", e)
        }
    }
}