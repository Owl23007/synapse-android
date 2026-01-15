package top.contins.synapse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.local.entity.chat.ConversationEntity
import top.contins.synapse.data.local.entity.chat.MessageEntity

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getConversations():@JvmSuppressWildcards Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversation(id: String):@JvmSuppressWildcards ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity):@JvmSuppressWildcards Long

    @Query("UPDATE conversations SET title = :title WHERE id = :id")
    suspend fun updateConversationTitle(id: String, title: String):@JvmSuppressWildcards Int

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String):@JvmSuppressWildcards Int

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String):@JvmSuppressWildcards Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity):@JvmSuppressWildcards Long
}
