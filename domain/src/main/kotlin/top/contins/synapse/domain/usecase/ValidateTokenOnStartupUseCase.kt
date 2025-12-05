package top.contins.synapse.domain.usecase

import top.contins.synapse.domain.model.AuthResult
import top.contins.synapse.domain.model.TokenValidationResult
import top.contins.synapse.domain.repository.AuthRepository
import top.contins.synapse.domain.repository.TokenRepository
import javax.inject.Inject

class ValidateTokenOnStartupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) {
    suspend operator fun invoke(): TokenValidationResult {
        if (!tokenRepository.hasValidTokens()) {
            return TokenValidationResult.NoTokens
        }

        val result = authRepository.checkAuth()
        return if (result is AuthResult.Success) {
            TokenValidationResult.Refreshed
        } else {
            TokenValidationResult.Invalid
        }
    }
}