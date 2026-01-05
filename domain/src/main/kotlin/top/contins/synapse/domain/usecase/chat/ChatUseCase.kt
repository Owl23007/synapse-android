package top.contins.synapse.domain.usecase.chat

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import top.contins.synapse.network.api.TokenProvider
import top.contins.synapse.network.api.ApiManager
import top.contins.synapse.network.model.ChatMessage
import top.contins.synapse.network.model.ChatRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 聊天相关的用例
 */
@Singleton
class ChatUseCase @Inject constructor(
    private val apiManager: ApiManager,
    private val tokenProvider: TokenProvider
) {
    
    /**
     * 发送消息到AI服务
     * @param message 用户输入的消息
     * @param conversationHistory 对话历史（可选）
     * @return AI的回复，如果失败返回错误信息
     */
    suspend fun sendMessage(message: String, conversationHistory: List<ChatMessage> = emptyList()): String {
        return try {
            // 从TokenProvider获取服务器端点
            val serverEndpoint = tokenProvider.getServerEndpoint() ?: throw IllegalStateException("用户未登录或服务器端点未设置")

            Log.d("ChatUseCase", "Sending message to: $serverEndpoint")
            
            // 确保API管理器已初始化
            if (!apiManager.isInitialized() || apiManager.getCurrentBaseUrl() != serverEndpoint) {
                apiManager.initializeWithBaseUrl(serverEndpoint)
            }
            
            val apiService = apiManager.getApiService()
            
            // 构建聊天请求
            val messages = conversationHistory + ChatMessage(role = "user", content = message)
            val chatRequest = ChatRequest(
                messages = messages,
                model = "default" // 可以后续改为可配置的模型
            )
            
            // 发起聊天请求
            val taskResponse = apiService.startChat(chatRequest)

            if (taskResponse.code == 0 && taskResponse.data != null) {
                val taskId = taskResponse.data!!.taskId
                Log.d("ChatUseCase", "Chat task started with ID: $taskId")
                
                // 获取流式响应
                val streamUrl = "$serverEndpoint/api/synapse/chat/$taskId"
                val response = apiService.streamChat(taskId)

                if (response.isSuccessful && response.body() != null) {
                    // 解析Server-Sent Events格式的流式响应
                    val fullResponse = withContext(Dispatchers.IO) {
                        parseStreamResponse(response.body()!!)
                    }
                    
                    // 停止聊天任务
                    try {
                        apiService.stopChat(taskId)
                    } catch (e: Exception) {
                        Log.w("ChatUseCase", "Failed to stop chat task", e)
                    }
                    
                    return fullResponse
                } else {
                    Log.e("ChatUseCase", "Stream request failed: ${response.code()}")
                    return "获取AI响应失败"
                }
            } else {
                Log.e("ChatUseCase", "Failed to start chat: ${taskResponse.message}")
                return "AI服务响应异常: ${taskResponse.message}"
            }
            
        } catch (e: Exception) {
            Log.e("ChatUseCase", "Error sending message", e)
            "发送消息失败，请检查网络连接: ${e.message}"
        }
    }
    
    /**
     * 检查AI服务是否可用
     */
    fun isAiServiceAvailable(): Boolean {
        return true
    }
    
    /**
     * 获取支持的AI模型列表
     */
    suspend fun getSupportedModels(): List<String> {
        return try {
            val serverEndpoint = tokenProvider.getServerEndpoint() ?: throw IllegalStateException("用户未登录或服务器端点未设置")

            // 确保API管理器已初始化
            if (!apiManager.isInitialized() || apiManager.getCurrentBaseUrl() != serverEndpoint) {
                apiManager.initializeWithBaseUrl(serverEndpoint)
            }
            
            val apiService = apiManager.getApiService()
            
            val response = apiService.getSupportedModels()

            if (response.code == 0 && response.data != null) {
                response.data!!.map { it.id }
            } else {
                Log.e("ChatUseCase", "Failed to get models: ${response.message}")
                listOf("default")
            }
        } catch (e: Exception) {
            Log.e("ChatUseCase", "Error getting models", e)
            listOf("default")
        }
    }
    
    /**
     * 获取AI服务状态信息
     */
    fun getAiServiceStatus(): String {
        return "AI服务已连接"
    }
    
    /**
     * 解析Server-Sent Events格式的流式响应
     */
    private fun parseStreamResponse(responseBody: ResponseBody): String {
        return try {
            val reader = BufferedReader(InputStreamReader(responseBody.byteStream(), "UTF-8"))
            val result = StringBuilder()
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    // Server-Sent Events格式：data: 内容
                    if (line.startsWith("data: ")) {
                        val content = line.substring(6) // 移除"data: "前缀
                        if (content.isNotEmpty()) {
                            result.append(content)
                        }
                    }
                }
            }
            
            val finalResult = result.toString().trim()
            Log.d("ChatUseCase", "Parsed stream response: $finalResult")

            finalResult.ifEmpty {
                "AI暂时没有回复内容"
            }
        } catch (e: Exception) {
            Log.e("ChatUseCase", "Error parsing stream response", e)
            "解析AI响应时出错: ${e.message}"
        }
    }
}