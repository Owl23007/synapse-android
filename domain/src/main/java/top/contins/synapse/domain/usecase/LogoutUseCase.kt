package top.contins.synapse.domain.usecase

import top.contins.synapse.data.storage.TokenManager
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {
    operator fun invoke() {
        tokenManager.clearTokens()
    }
}