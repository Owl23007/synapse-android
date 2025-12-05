package top.contins.synapse.feature.chat

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.core.ui.compose.markdown.MarkdownMessageItem
import kotlinx.coroutines.delay

data class Message(
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedText(): String {
        if (isUser) return text
        Log.d("Message", "【预处理前】\n$text")
        val result = preprocessMarkdown(text)
        Log.d("Message", "【预处理后】\n$result")
        return result
    }

    /**
     * 预处理 Markdown 文本
     * 保持原始格式，不做额外处理
     */
    private fun preprocessMarkdown(content: String): String {
        // replace "data: "with ’‘’
        return content
    }
}
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val serviceStatus by viewModel.serviceStatus.collectAsState()

    val listState = rememberLazyListState()

    // 优化滚动逻辑：减少频繁滚动，改善用户体验
    LaunchedEffect(messages.size) {
        if (messages.isEmpty()) return@LaunchedEffect

        // 只在消息数量变化时（新消息添加）滚动到底部
        delay(100) // 延迟以确保布局信息已更新
        if (listState.layoutInfo.totalItemsCount > 0) {
            // 计算滚动到底部所需的偏移量：总内容高度 - 可视区域高度
            val targetOffset = listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset
            listState.scrollToItem(messages.size - 1, targetOffset)
        }
    }
    
    // 监听最后一条消息的内容变化，但控制滚动频率
    var lastScrollTime by remember { mutableLongStateOf(0L) }
    LaunchedEffect(messages.lastOrNull()?.text?.length) {
        val lastMessage = messages.lastOrNull()
        val currentTime = System.currentTimeMillis()
        
        // 如果是流式消息且距离上次滚动超过500ms，才进行滚动
        if (lastMessage?.isStreaming == true && currentTime - lastScrollTime > 500) {
            lastScrollTime = currentTime
            delay(50)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.scrollToItem(messages.size - 1) // 使用scrollToItem而不是animateScrollToItem，减少动画开销
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 服务状态显示
        if (serviceStatus.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = serviceStatus,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // 聊天消息列表
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "还没有对话哦～",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "输入消息，和 AI 开始聊天吧！",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // 👇 修复：包裹 SelectionContainer 使 Markdown 内容可复制
                items(messages) { message ->
                    SelectionContainer {
                        MarkdownMessageItem(
                            content = message.getFormattedText(),
                            isUser = message.isUser,
                            isStreaming = message.isStreaming
                        )
                    }
                }
            }
        }

        // 输入框 + 发送按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = viewModel::updateInputText,
                placeholder = { Text("输入消息...") },
                modifier = Modifier.weight(1f).heightIn(max = 320.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = viewModel::sendMessage,
                modifier = Modifier.size(48.dp),
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
                }
            }
        }
    }
}