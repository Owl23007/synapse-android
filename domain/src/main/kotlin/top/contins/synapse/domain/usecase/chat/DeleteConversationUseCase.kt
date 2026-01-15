package top.contins.synapse.domain.usecase.chat

import top.contins.synapse.domain.repository.chat.ChatRepository
import javax.inject.Inject

class DeleteConversationUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String) {
        repository.deleteConversation(conversationId)
    }
}
