package top.contins.synapse.feature.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.contins.synapse.domain.usecase.ChatUseCase
import javax.inject.Inject

@HiltViewModel
class WritingViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase
) : ViewModel() {

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _evaluationResult = MutableStateFlow("")
    val evaluationResult: StateFlow<String> = _evaluationResult.asStateFlow()

    /**
     * 使用AI评判文章
     */
    suspend fun evaluateArticle(title: String, content: String): String {
        return try {
            _isEvaluating.value = true
            
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

            chatUseCase.sendMessage(prompt)
        } catch (e: Exception) {
            "AI评判过程中出现错误：${e.message ?: "网络连接失败，请稍后重试"}"
        } finally {
            _isEvaluating.value = false
        }
    }
}