package top.contins.synapse.domain.usecase.auth

import top.contins.synapse.domain.model.auth.AuthResult
import top.contins.synapse.domain.model.auth.User
import top.contins.synapse.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        identifier: String,
        password: String,
        serverEndpoint: String
    ): AuthResult<User> {
        // 验证输入参数
        if (identifier.isBlank()) {
            return AuthResult.Error("请输入用户名或邮箱")
        }
        
        if (password.isBlank()) {
            return AuthResult.Error("请输入密码")
        }
        
        if (serverEndpoint.isBlank()) {
            return AuthResult.Error("请输入服务器地址")
        }
        
        // 执行登录
        return authRepository.login(identifier, password, serverEndpoint)
    }
}