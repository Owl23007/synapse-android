package top.contins.synapse.data.local.entity.chat

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import top.contins.synapse.domain.model.chat.Message

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val role: String, // Stored as STRING
    val timestamp: Long
)

fun MessageEntity.toDomain() = Message(
    id = id,
    conversationId = conversationId,
    content = content,
    role = Message.Role.valueOf(role),
    timestamp = timestamp
)

fun Message.toEntity() = MessageEntity(
    id = id,
    conversationId = conversationId,
    content = content,
    role = role.name,
    timestamp = timestamp
)
