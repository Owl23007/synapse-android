package top.contins.synapse.network.model

import com.google.gson.annotations.SerializedName

/**
 * 聊天消息
 */
data class ChatMessage(
    @SerializedName("role")
    val role: String, // user, assistant, system
    @SerializedName("content")
    val content: String
)

/**
 * 聊天请求
 */
data class ChatRequest(
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    @SerializedName("model")
    val model: String,
    @SerializedName("taskId")
    val taskId: String? = null
)

/**
 * 任务响应
 */
data class TaskResponse(
    @SerializedName("taskId")
    val taskId: String
)

/**
 * 模型信息
 */
data class ModelInfo(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null
)

/**
 * 模型列表响应
 */
data class ChatModels(
    @SerializedName("models")
    val models: List<ModelInfo>
)

data class StreamResponse(
    @SerializedName("choices") val choices: List<ChatChoice>?,
    @SerializedName("type") val type: String?,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>?
)

data class ChatChoice(
    @SerializedName("delta") val delta: ChatDelta
)

data class ChatDelta(
    @SerializedName("content") val content: String?,
    @SerializedName("tool_calls") val toolCalls: List<ToolCall>?
)

data class ToolCall(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("function") val function: ToolCallFunction?
)

data class ToolCallFunction(
    @SerializedName("name") val name: String?,
    @SerializedName("arguments") val arguments: String?
)
