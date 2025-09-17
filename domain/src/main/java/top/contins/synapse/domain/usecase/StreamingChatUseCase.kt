package top.contins.synapse.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import top.contins.synapse.domain.service.RouteManager
import top.contins.synapse.network.api.ApiService
import top.contins.synapse.network.model.ChatMessage
import top.contins.synapse.network.model.ChatRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 流式聊天用例 - 支持实时显示AI回复
 */
@Singleton
class StreamingChatUseCase @Inject constructor(
    private val routeManager: RouteManager,
    private val apiService: ApiService
) {

    /**
     * 发送消息并获取流式响应
     * @param message 用户输入的消息
     * @param conversationHistory 对话历史
     * @return 流式响应Flow，每次emit一个新的文本片段
     */
    suspend fun sendMessageStream(
        message: String,
        conversationHistory: List<ChatMessage> = emptyList()
    ): Flow<String> = flow {
        // 获取 synapse AI 服务的端点
        val synapseEndpoint = routeManager.getServiceEndpoint("synapse")

        if (synapseEndpoint == null) {
            Log.e("StreamingChatUseCase", "Synapse service endpoint not found")
            emit("AI服务暂时不可用，请稍后重试")
            return@flow
        }

        // 构建聊天请求
        val messages = conversationHistory + ChatMessage(role = "user", content = message)
        val chatRequest = ChatRequest(
            messages = messages,
            model = "default"
        )

        // 发起聊天请求
        val chatUrl = "$synapseEndpoint/chat"
        val taskResponse = withContext(Dispatchers.IO) {
            apiService.startChat(chatUrl, chatRequest)
        }

        if (taskResponse.code == 0 && taskResponse.data != null) {
            val taskId = taskResponse.data!!.taskId
            Log.d("StreamingChatUseCase", "Chat task started with ID: $taskId")

            // 获取流式响应
            val streamUrl = "$synapseEndpoint/chat/$taskId"
            val response = withContext(Dispatchers.IO) {
                apiService.streamChat(streamUrl)
            }

            if (response.isSuccessful && response.body() != null) {
                // 解析Server-Sent Events格式的流式响应
                emitAll(parseStreamResponseFlow(response.body()!!))

                // 停止聊天任务
                val stopUrl = "$synapseEndpoint/chat/$taskId"
                try {
                    withContext(Dispatchers.IO) {
                        apiService.stopChat(stopUrl)
                    }
                } catch (e: Exception) {
                    Log.w("StreamingChatUseCase", "Failed to stop chat task", e)
                }
            } else {
                Log.e("StreamingChatUseCase", "Stream request failed: ${response.code()}")
                emit("获取AI响应失败")
            }
        } else {
            Log.e("StreamingChatUseCase", "Failed to start chat: ${taskResponse.message}")
            emit("AI服务响应异常: ${taskResponse.message}")
        }
    }.catch { e ->
        Log.e("StreamingChatUseCase", "Error sending message", e)
        emit("发送消息失败，请检查网络连接: ${e.message}")
    }.flowOn(Dispatchers.IO)

    /**
     * 解析Server-Sent Events格式的流式响应为Flow
     * 让每个HTTP chunk直接传递给UI，避免重新组装
     */
    private fun parseStreamResponseFlow(responseBody: okhttp3.ResponseBody): Flow<String> = flow {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), "UTF-8"))

        try {
            // 逐行读取，但不等待完整响应
            reader.useLines { lineSequence ->
                for (line in lineSequence) {
                    Log.d("StreamingChatUseCase", "Received line: '$line'")

                    // 检查行是否以 "data: " 开头
                    if (line.startsWith("data: ")) {
                        val content = line.substring(6) // 安全地移除"data: "前缀

                        // 检查是否为结束标志
                        if (content == "[DONE]") {
                            Log.d("StreamingChatUseCase", "Stream completed with [DONE] signal")
                            return@flow
                        }

                        // 直接追加原始内容
                        if (content.isNotEmpty()) {
                            emit(content)
                        }
                    }
                    // 处理空行 (SSE消息分隔符)
                    else if (line.isEmpty()) {
                        Log.d("StreamingChatUseCase", "SSE message boundary detected")
                        // 空行是SSE协议的一部分，用于分隔消息，可以忽略
                        emit("\n")
                    }
                    else {
                        emit(line)
                    }
                }
            }

        } finally {
            reader.close()
        }
    }.catch { e ->
        Log.e("StreamingChatUseCase", "Error parsing stream response", e)
        emit("解析AI响应时出错: ${e.message}")
    }.flowOn(Dispatchers.IO)
}