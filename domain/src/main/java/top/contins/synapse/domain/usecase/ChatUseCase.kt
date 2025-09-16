package top.contins.synapse.domain.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
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
 * AI 聊天相关的用例
 */
@Singleton
class ChatUseCase @Inject constructor(
    private val routeManager: RouteManager,
    private val apiService: ApiService
) {
    
    /**
     * 发送消息到AI服务
     * @param message 用户输入的消息
     * @param conversationHistory 对话历史（可选）
     * @return AI的回复，如果失败返回错误信息
     */
    suspend fun sendMessage(message: String, conversationHistory: List<ChatMessage> = emptyList()): String {
        return try {
            // 获取 synapse AI 服务的端点
            val synapseEndpoint = routeManager.getServiceEndpoint("synapse")
            
            if (synapseEndpoint == null) {
                Log.e("ChatUseCase", "Synapse service endpoint not found")
                return "AI服务暂时不可用，请稍后重试"
            }
            
            Log.d("ChatUseCase", "Sending message to: $synapseEndpoint")
            
            // 构建聊天请求
            val messages = conversationHistory + ChatMessage(role = "user", content = message)
            val chatRequest = ChatRequest(
                messages = messages,
                model = "default" // 可以后续改为可配置的模型
            )
            
            // 发起聊天请求
            val chatUrl = "$synapseEndpoint/chat"
            val taskResponse = apiService.startChat(chatUrl, chatRequest)
            
            if (taskResponse.code == 0 && taskResponse.data != null) {
                val taskId = taskResponse.data!!.taskId
                Log.d("ChatUseCase", "Chat task started with ID: $taskId")
                
                // 获取流式响应
                val streamUrl = "$synapseEndpoint/chat/$taskId"
                val response = apiService.streamChat(streamUrl)
                
                if (response.isSuccessful && response.body() != null) {
                    // 解析Server-Sent Events格式的流式响应
                    val fullResponse = withContext(Dispatchers.IO) {
                        parseStreamResponse(response.body()!!)
                    }
                    
                    // 停止聊天任务
                    val stopUrl = "$synapseEndpoint/chat/$taskId"
                    try {
                        apiService.stopChat(stopUrl)
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
        return routeManager.getServiceEndpoint("synapse") != null
    }
    
    /**
     * 获取支持的AI模型列表
     */
    suspend fun getSupportedModels(): List<String> {
        return try {
            val synapseEndpoint = routeManager.getServiceEndpoint("synapse")
            if (synapseEndpoint == null) {
                Log.e("ChatUseCase", "Synapse service endpoint not found")
                return emptyList()
            }
            
            val modelsUrl = "$synapseEndpoint/models"
            val response = apiService.getSupportedModels(modelsUrl)
            
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
        val endpoint = routeManager.getServiceEndpoint("synapse")
        return if (endpoint != null) {
            "AI服务已连接: $endpoint"
        } else {
            "AI服务未连接"
        }
    }
    
    /**
     * 解析Server-Sent Events格式的流式响应
     */
    private fun parseStreamResponse(responseBody: okhttp3.ResponseBody): String {
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
            
            if (finalResult.isEmpty()) {
                "AI暂时没有回复内容"
            } else {
                finalResult
            }
        } catch (e: Exception) {
            Log.e("ChatUseCase", "Error parsing stream response", e)
            "解析AI响应时出错: ${e.message}"
        }
    }
}