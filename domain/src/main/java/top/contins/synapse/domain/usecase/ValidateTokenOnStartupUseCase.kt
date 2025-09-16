package top.contins.synapse.domain.usecase

import top.contins.synapse.network.service.TokenValidationResult
import top.contins.synapse.network.service.TokenValidationService
import javax.inject.Inject

class ValidateTokenOnStartupUseCase @Inject constructor(
    private val tokenValidationService: TokenValidationService
) {
    suspend operator fun invoke(): TokenValidationResult {
        return tokenValidationService.validateTokenOnStartup()
    }
}