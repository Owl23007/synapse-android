package top.contins.synapse.feature.assistant

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.chat.Conversation
import top.contins.synapse.domain.model.chat.Message
import top.contins.synapse.domain.usecase.chat.*
import top.contins.synapse.network.model.ChatMessage
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val saveMessageUseCase: SaveMessageUseCase,
    private val deleteConversationUseCase: DeleteConversationUseCase,
    private val streamingChatUseCase: StreamingChatUseCase
) : ViewModel() {

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    val conversations: StateFlow<List<Conversation>> = getConversationsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _streamingMessage = MutableStateFlow<Message?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<Message>> = _currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else getMessagesUseCase(id)
        }
        .combine(_streamingMessage) { dbMessages, streamingMsg ->
            if (streamingMsg != null) {
                dbMessages + streamingMsg
            } else {
                dbMessages
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun startNewChat() {
        _currentConversationId.value = null
        _streamingMessage.value = null
    }

    fun loadConversation(conversation: Conversation) {
        _currentConversationId.value = conversation.id
        _streamingMessage.value = null
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            deleteConversationUseCase(conversationId)
            if (_currentConversationId.value == conversationId) {
                startNewChat()
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _inputText.value = ""

                // Ensure Conversation Exists
                val conversationId = _currentConversationId.value ?: run {
                    val newConv = createConversationUseCase(
                        title = text.take(20) + if (text.length > 20) "..." else ""
                    )
                    _currentConversationId.value = newConv.id
                    newConv.id
                }

                // Save User Message
                val userMessage = Message(
                    id = UUID.randomUUID().toString(),
                    conversationId = conversationId,
                    content = text,
                    role = Message.Role.USER,
                    timestamp = System.currentTimeMillis()
                )
                saveMessageUseCase(userMessage)
                
                val currentMessages = messages.value
                val historyContext = currentMessages.map { 
                     ChatMessage(
                         role = if (it.role == Message.Role.USER) "user" else "assistant",
                         content = it.content
                     )
                }.takeLast(10).toMutableList()

                if (historyContext.none { it.content == text }) {
                     historyContext.add(ChatMessage("user", text))
                }

                var aiContent = ""
                val aiMessageId = UUID.randomUUID().toString()
                
                // Update Streaming State
                val initialStreamingMsg = Message(
                    id = aiMessageId,
                    conversationId = conversationId,
                    content = "...", // Typing indicator
                    role = Message.Role.ASSISTANT,
                    timestamp = System.currentTimeMillis(),
                    isStreaming = true
                )
                _streamingMessage.value = initialStreamingMsg

                streamingChatUseCase.sendMessageStream(text, historyContext).collect { chunk ->
                    aiContent += chunk
                    _streamingMessage.value = initialStreamingMsg.copy(
                        content = aiContent,
                        isStreaming = true
                    )
                }

                // Save Final AI Message
                val finalAiMessage = Message(
                    id = aiMessageId,
                    conversationId = conversationId,
                    content = aiContent,
                    role = Message.Role.ASSISTANT,
                    timestamp = System.currentTimeMillis(),
                    isStreaming = false
                )
                saveMessageUseCase(finalAiMessage)
                _streamingMessage.value = null // Clear streaming state

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                val conversationId = _currentConversationId.value
                if (conversationId != null) {
                    val errorMessage = Message(
                        id = UUID.randomUUID().toString(),
                        conversationId = conversationId,
                        content = "Sorry, something went wrong. Please try again.",
                        role = Message.Role.ASSISTANT
                    )
                    // Optionally save error message or just show in UI
                    _streamingMessage.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
