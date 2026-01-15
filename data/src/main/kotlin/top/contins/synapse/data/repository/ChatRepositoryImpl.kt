package top.contins.synapse.data.repository.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.dao.ChatDao
import top.contins.synapse.data.local.entity.chat.toDomain
import top.contins.synapse.data.local.entity.chat.toEntity
import top.contins.synapse.domain.model.chat.Conversation
import top.contins.synapse.domain.model.chat.Message
import top.contins.synapse.domain.repository.chat.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getConversations(): Flow<List<Conversation>> {
        return chatDao.getConversations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getMessages(conversationId: String): Flow<List<Message>> {
        return chatDao.getMessages(conversationId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getConversation(id: String): Conversation? {
        return chatDao.getConversation(id)?.toDomain()
    }

    override suspend fun saveConversation(conversation: Conversation) {
        chatDao.insertConversation(conversation.toEntity())
    }

    override suspend fun saveMessage(message: Message) {
        chatDao.insertMessage(message.toEntity())
    }

    override suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
    }

    override suspend fun updateConversationTitle(conversationId: String, title: String) {
        chatDao.updateConversationTitle(conversationId, title)
    }
}
