package top.contins.synapse.domain.model.chat

data class Conversation(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
