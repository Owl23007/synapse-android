package top.contins.synapse.domain.model.chat

data class Message(
    val id: String,
    val conversationId: String,
    val content: String,
    val role: Role,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
) {
    enum class Role {
        USER, ASSISTANT, SYSTEM
    }
}
