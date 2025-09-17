package top.contins.synapse.ui.compose.markdown

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.contins.synapse.ui.screens.tabs.Message

@Composable
fun MarkdownMessageItem(message: Message) {
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val textAlign = if (message.isUser) TextAlign.End else TextAlign.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .widthIn(max = 320.dp) // 限制最大宽度
        ) {
            SelectionContainer {
                Box(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (message.isUser) {
                        // 用户消息使用普通文本
                        Text(
                            text = message.text,
                            color = textColor,
                            fontSize = 16.sp,
                            textAlign = textAlign,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        // AI回复使用Markdown渲染，使用格式化后的文本

                        Column {
                            val markdownText = message.getFormattedText()
                            MarkdownRenderer(markdown = markdownText)
                            //显示markdown内容
                            // 如果消息正在流式传输，显示打字指示器
                            if (message.isStreaming) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(3) { index ->
                                        val alpha by rememberInfiniteTransition(label = "typing").animateFloat(
                                            initialValue = 0.3f,
                                            targetValue = 1f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(
                                                    durationMillis = 600,
                                                    delayMillis = index * 200
                                                ),
                                                repeatMode = RepeatMode.Reverse
                                            ), label = "typing_alpha"
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .padding(horizontal = 1.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = textColor.copy(alpha = alpha),
                                                modifier = Modifier.fillMaxSize()
                                            ) {}
                                        }
                                    }
                                    
                                    // 添加"AI正在思考..."文本
                                    Text(
                                        text = "AI正在思考...",
                                        color = textColor.copy(alpha = 0.6f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
