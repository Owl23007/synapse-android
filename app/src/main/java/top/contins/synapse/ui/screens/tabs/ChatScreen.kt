package top.contins.synapse.ui.screens.tabs

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
import top.contins.synapse.ui.components.MarkdownMessageItem
import top.contins.synapse.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

data class Message(
    val text: String,
    val isUser: Boolean,
    val isStreaming: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 预处理消息文本，确保 Markdown 格式正确
     */
    fun getFormattedText(): String {
        if (isUser) return text

        // 直接返回原始内容，暂不预处理
        return preprocessMarkdown(text)
    }

    /**
     * 暂不预处理 Markdown，直接返回原始内容
     * 如需格式规范化，可在此处添加逻辑
     */
    private fun preprocessMarkdown(content: String) = content
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

    // 优化滚动逻辑：在流式输出时也保持底部可见
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isEmpty()) return@LaunchedEffect

        val lastMessage = messages.last()
        
        // 如果是流式消息，每当内容增长时滚动到底部（但不要太频繁）
        if (lastMessage.isStreaming) {
            // 流式输出时，延迟较短，保持内容可见
            delay(50)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(messages.size - 1)
            }
        } else {
            // 消息完成时，确保滚动到底部
            delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                listState.animateScrollToItem(messages.size - 1)
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
                        MarkdownMessageItem(message = message)
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