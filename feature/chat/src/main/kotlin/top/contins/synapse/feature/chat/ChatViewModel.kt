package top.contins.synapse.feature.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.usecase.ChatUseCase
import top.contins.synapse.domain.usecase.StreamingChatUseCase
import top.contins.synapse.network.model.ChatMessage
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val streamingChatUseCase: StreamingChatUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _serviceStatus = MutableStateFlow("")
    val serviceStatus: StateFlow<String> = _serviceStatus.asStateFlow()

    init {
        // 初始化时显示AI服务状态
        updateServiceStatus()
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val messageText = _inputText.value.trim()
        if (messageText.isBlank() || _isLoading.value) {
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // 添加用户消息
                _messages.value = _messages.value + Message(messageText, isUser = true)
                
                // 清空输入框
                _inputText.value = ""
                
                // 构建对话历史（转换为网络模型格式）
                val conversationHistory = _messages.value.dropLast(1).map { message ->
                    ChatMessage(
                        role = if (message.isUser) "user" else "assistant",
                        content = message.text
                    )
                }
                
                // 添加一个空的AI消息占位符（标记为正在流式传输）
                val aiMessageIndex = _messages.value.size
                _messages.value = _messages.value + Message("", isUser = false, isStreaming = true)
                Log.d("ChatViewModel", "Added placeholder message at index: $aiMessageIndex")
                
                // 使用流式响应
                var chunkCount = 0
                var fullResponse = ""
                streamingChatUseCase.sendMessageStream(messageText, conversationHistory).collect { chunk ->
                    chunkCount++
                    val currentTime = System.currentTimeMillis()
                    fullResponse += chunk
                    // 实时更新AI消息内容
                    val currentMessages = _messages.value.toMutableList()
                    if (aiMessageIndex < currentMessages.size) {
                        val updatedMessage = Message(fullResponse, isUser = false, isStreaming = true)
                        currentMessages[aiMessageIndex] = updatedMessage
                        
                        // 强制触发状态更新
                        _messages.value = currentMessages.toList()


                        Log.d("ChatViewModel", "Updated message at index $aiMessageIndex with ${chunk.length} characters (time: $currentTime)")
                    } else {
                        Log.e("ChatViewModel", "Invalid aiMessageIndex: $aiMessageIndex, messages size: ${currentMessages.size}")
                    }
                }
                
                // 流式传输完成，移除流式传输标识
                val finalMessages = _messages.value.toMutableList()
                if (aiMessageIndex < finalMessages.size) {
                    val finalMessage = finalMessages[aiMessageIndex]
                    finalMessages[aiMessageIndex] = finalMessage.copy(isStreaming = false)
                    _messages.value = finalMessages
                    Log.d("ChatViewModel", "Completed streaming for message at index $aiMessageIndex")
                }
                
            } catch (e: Exception) {
                // 添加错误消息
                val currentMessages = _messages.value.toMutableList()
                val errorMessage = Message("抱歉，发生了错误，请稍后重试", isUser = false, isStreaming = false)
                
                // 如果有占位符消息，替换它；否则添加新消息
                if (currentMessages.isNotEmpty() && !currentMessages.last().isUser) {
                    currentMessages[currentMessages.size - 1] = errorMessage
                } else {
                    currentMessages.add(errorMessage)
                }
                _messages.value = currentMessages
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateServiceStatus() {
        _serviceStatus.value = chatUseCase.getAiServiceStatus()
    }

    fun refreshServiceStatus() {
        updateServiceStatus()
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }
}