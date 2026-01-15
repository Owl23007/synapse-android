package top.contins.synapse.domain.usecase.chat

import top.contins.synapse.domain.model.chat.Conversation
import top.contins.synapse.domain.repository.chat.ChatRepository
import java.util.UUID
import javax.inject.Inject

class CreateConversationUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(title: String = "New Chat"): Conversation {
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.saveConversation(conversation)
        return conversation
    }
}
