package top.contins.synapse.domain.usecase

import top.contins.synapse.data.storage.TokenManager
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {
    operator fun invoke(): Boolean {
        return tokenManager.hasValidTokens()
    }
}