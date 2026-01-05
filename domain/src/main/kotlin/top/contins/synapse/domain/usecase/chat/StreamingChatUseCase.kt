package top.contins.synapse.domain.usecase.chat

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.domain.usecase.schedule.CreateScheduleUseCase
import top.contins.synapse.network.api.TokenProvider
import top.contins.synapse.network.api.ApiManager
import top.contins.synapse.network.model.ChatMessage
import top.contins.synapse.network.model.ChatRequest
import top.contins.synapse.network.model.StreamResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 流式聊天用例 - 支持实时显示AI回复
 */
@Singleton
class StreamingChatUseCase @Inject constructor(
    private val apiManager: ApiManager,
    private val tokenProvider: TokenProvider,
    private val createScheduleUseCase: CreateScheduleUseCase
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
        // 从TokenProvider获取服务器端点
        val serverEndpoint = tokenProvider.getServerEndpoint() ?: throw IllegalStateException("用户未登录或服务器端点未设置")

        // 确保API管理器已初始化
        if (!apiManager.isInitialized() || apiManager.getCurrentBaseUrl() != serverEndpoint) {
            apiManager.initializeWithBaseUrl(serverEndpoint)
        }
        
        val apiService = apiManager.getApiService()

        // 构建聊天请求
        val messages = conversationHistory + ChatMessage(role = "user", content = message)
        val chatRequest = ChatRequest(
            messages = messages,
            model = "default"
        )

        // 发起聊天请求
        val taskResponse = withContext(Dispatchers.IO) {
            apiService.startChat(chatRequest)
        }

        if (taskResponse.code == 0 && taskResponse.data != null) {
            val taskId = taskResponse.data!!.taskId
            Log.d("StreamingChatUseCase", "Chat task started with ID: $taskId")

            // 获取流式响应
            val response = withContext(Dispatchers.IO) {
                apiService.streamChat(taskId)
            }

            if (response.isSuccessful && response.body() != null) {
                // 解析Server-Sent Events格式的流式响应
                emitAll(parseStreamResponseFlow(response.body()!!))

                // 停止聊天任务
                try {
                    withContext(Dispatchers.IO) {
                        apiService.stopChat(taskId)
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
     *我该复习什么内容   | 我今天晚上6.00-8.00 计算机网络考试*/
    private fun parseStreamResponseFlow(responseBody: ResponseBody): Flow<String> = flow {
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), "UTF-8"))
        val gson = Gson()

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

                        try {
                            val streamResponse = gson.fromJson(content, StreamResponse::class.java)

                            // 处理文本内容
                            val textContent = streamResponse.choices?.firstOrNull()?.delta?.content
                            if (!textContent.isNullOrEmpty()) {
                                emit(textContent)
                            }

                            // 处理工具调用请求 (服务端无法处理时)
                            if (streamResponse.type == "tool_request") {
                                streamResponse.toolCalls?.forEach { toolCall ->
                                    if (toolCall.function?.name == "create_schedule") {
                                        handleCreateScheduleTool(toolCall.function?.arguments)
                                        emit("\n[已为您创建日程]")
                                    }
                                }
                            }

                            // 处理流式工具调用 (如果服务端返回的是 delta tool_calls)
                             streamResponse.choices?.firstOrNull()?.delta?.toolCalls?.forEach { toolCall ->
                                // 这里通常需要累积参数，简化起见假设一次性返回或服务端已处理
                                // 实际场景中可能需要更复杂的流式参数拼接逻辑
                                // 如果后端直接返回 tool_request 类型会更简单
                            }

                        } catch (e: Exception) {
                            // 如果解析JSON失败，尝试直接作为文本输出（兼容旧格式）
                            if (content.isNotEmpty()) {
                                emit(content)
                            }
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

    private suspend fun handleCreateScheduleTool(argumentsJson: String?) {
        if (argumentsJson.isNullOrEmpty()) return

        try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val args: Map<String, Any> = gson.fromJson(argumentsJson, type)
            
            val title = args["summary"] as? String ?: "未命名日程"
            val startTimeStr = args["startTime"] as? String
            val endTimeStr = args["endTime"] as? String
            val location = args["location"] as? String
            val description = args["description"] as? String

            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val startTime = if (startTimeStr != null) LocalDateTime.parse(startTimeStr, formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() else System.currentTimeMillis()
            val endTime = if (endTimeStr != null) LocalDateTime.parse(endTimeStr, formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() else startTime + 3600000 // 默认1小时

            val schedule = Schedule(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                startTime = startTime,
                endTime = endTime,
                timezoneId = ZoneId.systemDefault().id,
                location = location,
                type = ScheduleType.WORK, // 默认为工作
                calendarId = "default", // 默认日历
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            createScheduleUseCase(schedule)
            Log.d("StreamingChatUseCase", "Created schedule from tool call: $title")

        } catch (e: Exception) {
            Log.e("StreamingChatUseCase", "Failed to execute create_schedule tool", e)
        }
    }
}