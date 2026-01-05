package top.contins.synapse.domain.model.auth

data class CaptchaResponse(
    val captchaId: String,
    val captchaImageBase64: String
)