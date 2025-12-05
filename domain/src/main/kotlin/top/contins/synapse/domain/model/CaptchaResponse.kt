package top.contins.synapse.domain.model

data class CaptchaResponse(
    val captchaId: String,
    val captchaImageBase64: String
)