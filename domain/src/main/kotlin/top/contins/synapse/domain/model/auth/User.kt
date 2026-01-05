package top.contins.synapse.domain.model.auth

data class User(
    val id: Long? = null,
    val email: String,
    val username: String,
    val nickname: String,
    val avatar: String = "",
    val background: String = "",
    val signature: String = "",
    val serverEndpoint: String = ""
)