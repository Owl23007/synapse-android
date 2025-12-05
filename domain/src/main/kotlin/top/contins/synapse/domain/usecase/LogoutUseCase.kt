package top.contins.synapse.domain.usecase

import top.contins.synapse.domain.repository.TokenRepository
import top.contins.synapse.domain.service.RouteManager
import javax.inject.Inject

/**
 * 登出用例
 * 负责清理用户会话数据，包括token和路由信息
 */
class LogoutUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val routeManager: RouteManager
) {
    
    /**
     * 执行登出操作
     * 清除保存的token和路由信息
     */
    operator fun invoke() {
        // 清除保存的token
        tokenRepository.clearTokens()
        
        // 清除路由信息
        routeManager.clearRoutes()
    }
}