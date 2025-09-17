package top.contins.synapse.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import top.contins.synapse.data.repository.WritingRepository
import top.contins.synapse.domain.usecase.ChatUseCase
import top.contins.synapse.data.model.WritingContent
import javax.inject.Inject

/**
 * 写作功能的ViewModel
 */
@HiltViewModel
class WritingViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val repository: WritingRepository
) : ViewModel() {
    
    // 全局异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("WritingViewModel", "Uncaught coroutine exception", exception)
        showMessage("操作失败：${exception.message ?: "未知错误"}")
    }
    
    // 使用带异常处理器的协程作用域
    private val safeViewModelScope = viewModelScope + exceptionHandler
    
    private val _uiState = MutableStateFlow(WritingUiState())
    val uiState: StateFlow<WritingUiState> = _uiState.asStateFlow()
    
    private val _drafts = MutableStateFlow<List<WritingContent>>(emptyList())
    val drafts: StateFlow<List<WritingContent>> = _drafts.asStateFlow()
    
    private val _published = MutableStateFlow<List<WritingContent>>(emptyList())
    val published: StateFlow<List<WritingContent>> = _published.asStateFlow()
    
    init {
        loadWritings()
    }
    
    /**
     * 加载所有写作内容
     */
    private fun loadWritings() {
        safeViewModelScope.launch {
            try {
                repository.writings.collect { writings ->
                    _drafts.value = writings.filter { !it.isPublished }
                        .sortedByDescending { it.updatedAt }
                    _published.value = writings.filter { it.isPublished }
                        .sortedByDescending { it.updatedAt }
                }
            } catch (e: Exception) {
                Log.e("WritingViewModel", "Error loading writings", e)
                // 发生错误时设置为空列表
                _drafts.value = emptyList()
                _published.value = emptyList()
            }
        }
    }
    
    /**
     * 显示编辑器
     */
    fun showEditor(content: WritingContent? = null, template: String? = null) {
        val initialContent = content ?: WritingContent(
            template = template,
            title = when (template) {
                "日记" -> "今日记录"
                "工作总结" -> "工作总结 - ${getCurrentDate()}"
                "学习笔记" -> "学习笔记"
                "项目计划" -> "项目计划"
                "创意文案" -> "创意文案"
                else -> ""
            }
        )
        
        _uiState.value = _uiState.value.copy(
            showEditor = true,
            currentEditingContent = initialContent
        )
    }
    
    /**
     * 隐藏编辑器
     */
    fun hideEditor() {
        _uiState.value = _uiState.value.copy(
            showEditor = false,
            currentEditingContent = null
        )
    }
    
    /**
     * 保存写作内容
     */
    fun saveWriting(content: WritingContent) {
        viewModelScope.launch {
            try {
                val success = repository.saveWriting(content)
                if (success) {
                    showMessage("保存成功")
                } else {
                    showMessage("保存失败")
                }
            } catch (e: Exception) {
                Log.e("WritingViewModel", "Error saving writing", e)
                showMessage("保存失败：${e.message}")
            }
        }
    }
    
    /**
     * 发布写作内容
     */
    fun publishWriting(content: WritingContent) {
        viewModelScope.launch {
            try {
                val success = repository.publishWriting(content)
                if (success) {
                    hideEditor()
                    showMessage("发布成功")
                } else {
                    showMessage("发布失败")
                }
            } catch (e: Exception) {
                Log.e("WritingViewModel", "Error publishing writing", e)
                showMessage("发布失败：${e.message}")
            }
        }
    }
    
    /**
     * 删除写作内容
     */
    fun deleteWriting(content: WritingContent) {
        viewModelScope.launch {
            try {
                val success = repository.deleteWriting(content.id)
                if (success) {
                    showMessage("删除成功")
                } else {
                    showMessage("删除失败")
                }
            } catch (e: Exception) {
                Log.e("WritingViewModel", "Error deleting writing", e)
                showMessage("删除失败：${e.message}")
            }
        }
    }
    
    /**
     * 使用AI评判文章
     */
    suspend fun evaluateArticle(title: String, content: String): String {
        return try {
            Log.d("WritingViewModel", "Starting AI evaluation for article: $title")
            
            if (title.isBlank() && content.isBlank()) {
                return "请先输入文章标题和内容"
            }
            
            val prompt = """
                请作为一位专业的文章评判专家，对以下文章进行全面评价。请从以下几个维度进行分析：

                📊 **评分标准**（满分100分）：
                - 结构逻辑性（25分）：文章结构是否清晰，逻辑是否连贯
                - 语言表达（25分）：用词是否准确，表达是否流畅
                - 内容价值（25分）：观点是否有见地，内容是否充实
                - 创新性（25分）：是否有独特见解，是否有创新思维

                请按以下格式输出评价结果：

                **📊 综合评分：XX分**

                **✅ 主要优点：**
                • [具体优点1]
                • [具体优点2]
                • [具体优点3]

                **⚠️ 需要改进：**
                • [具体问题1及改进建议]
                • [具体问题2及改进建议]
                • [具体问题3及改进建议]

                **💡 写作建议：**
                • [针对性建议1]
                • [针对性建议2]
                • [针对性建议3]

                **🎯 总结：**
                [一句话总结这篇文章的整体水平和潜力]

                ---
                **文章标题：** $title
                **文章内容：**
                $content
            """.trimIndent()

            val result = chatUseCase.sendMessage(prompt)
            Log.d("WritingViewModel", "AI evaluation completed successfully")
            result
        } catch (e: Exception) {
            Log.e("WritingViewModel", "Error during AI evaluation", e)
            when (e) {
                is java.net.UnknownHostException -> "网络连接失败，请检查网络设置"
                is java.net.SocketTimeoutException -> "请求超时，请稍后重试"
                is java.io.IOException -> "网络错误，请稍后重试"
                else -> "AI评判过程中出现错误：${e.message ?: "未知错误"}"
            }
        }
    }
    
    /**
     * 切换标签页
     */
    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }
    
    /**
     * 显示消息
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
        // 3秒后清除消息
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(message = null)
        }
    }
    
    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

/**
 * 写作页面的UI状态
 */
data class WritingUiState(
    val selectedTab: Int = 0,
    val showEditor: Boolean = false,
    val currentEditingContent: WritingContent? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)