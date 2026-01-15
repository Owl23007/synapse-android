package top.contins.synapse.domain.usecase.chat

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.chat.Message
import top.contins.synapse.domain.repository.chat.ChatRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> = repository.getMessages(conversationId)
}
