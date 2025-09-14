package top.contins.synapse.domain.model

data class User(
    val id: Long? = null,

    val email: String,
    val password: String,
    val username: String,
    val nickname: String,

    val avatar: String,
    val background: String,
    val  signature: String,

    val accessToken: String,
    val refreshToken: String,


    val serverEndpoint: String
)