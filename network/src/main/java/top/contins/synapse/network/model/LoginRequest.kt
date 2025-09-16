package top.contins.synapse.network.model

data class LoginRequest(
    val account: String, // 可以是邮箱或用户名
    val password: String
)