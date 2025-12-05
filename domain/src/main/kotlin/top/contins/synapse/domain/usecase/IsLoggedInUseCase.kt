package top.contins.synapse.domain.usecase

import top.contins.synapse.domain.repository.TokenRepository
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val tokenRepository: TokenRepository
) {
    operator fun invoke(): Boolean {
        return tokenRepository.hasValidTokens()
    }
}