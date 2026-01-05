package top.contins.synapse.domain.usecase.auth

import top.contins.synapse.domain.repository.TokenRepository
import javax.inject.Inject

/**
 * 登出用例
 * 负责清理用户会话数据，包括token和路由信息
 */
class LogoutUseCase @Inject constructor(
    private val tokenRepository: TokenRepository
) {

    /**
     * 执行登出操作
     * 清除保存的token和路由信息
     */
    operator fun invoke() {
        // 清除保存的token
        tokenRepository.clearTokens()
    }
}
        
    