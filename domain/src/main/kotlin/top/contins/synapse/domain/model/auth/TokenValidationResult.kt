package top.contins.synapse.domain.model.auth

enum class TokenValidationResult {
    NoTokens,
    Refreshed,
    Invalid
}