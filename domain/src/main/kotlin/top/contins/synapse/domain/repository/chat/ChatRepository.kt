package top.contins.synapse.domain.repository.chat

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.chat.Conversation
import top.contins.synapse.domain.model.chat.Message

interface ChatRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getMessages(conversationId: String): Flow<List<Message>>
    suspend fun getConversation(id: String): Conversation?
    suspend fun saveConversation(conversation: Conversation)
    suspend fun saveMessage(message: Message)
    suspend fun deleteConversation(conversationId: String)
    suspend fun updateConversationTitle(conversationId: String, title: String)
}
