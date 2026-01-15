package top.contins.synapse.domain.usecase.chat

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.chat.Conversation
import top.contins.synapse.domain.repository.chat.ChatRepository
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.getConversations()
}
