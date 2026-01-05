package top.contins.synapse.domain.model.schedule

data class Subscription(
    val id: String,
    val name: String,
    val url: String,
    val color: Long?,
    val syncInterval: Int = 24,
    val lastSyncAt: Long? = null,
    val isEnabled: Boolean = true,
    val createdAt: Long
)
