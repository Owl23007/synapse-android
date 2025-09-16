package top.contins.synapse.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jeziellago.compose.markdowntext.MarkdownText
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
                .widthIn(max = 280.dp) // 限制最大宽度
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
                        // AI回复使用Markdown渲染
                        MarkdownText(
                            markdown = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}