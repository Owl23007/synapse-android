package top.contins.synapse.core.ui.compose.markdown

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography

@Composable
fun MarkdownMessageItem(
    content: String,
    isUser: Boolean,
    isStreaming: Boolean = false
) {
    val textColor = if (isUser) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
    if (isUser) {
        // 用户消息使用普通文本
        Text(
            text = content,
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        // AI回复使用Markdown渲染
        // 配置Markdown颜色以匹配应用主题
        val markdownColors = DefaultMarkdownColors(
            text = textColor,
            codeBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            inlineCodeBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            tableBackground = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )

        // 配置Markdown字体样式
        val markdownTypography = DefaultMarkdownTypography(
            h1 = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            h2 = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            h3 = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            h4 = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            h5 = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            h6 = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            paragraph = MaterialTheme.typography.bodyMedium,
            text = MaterialTheme.typography.bodyMedium,
            ordered = MaterialTheme.typography.bodyMedium,
            bullet = MaterialTheme.typography.bodyMedium,
            list = MaterialTheme.typography.bodyMedium,
            quote = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            ),
            code = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            inlineCode = MaterialTheme.typography.bodySmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            textLink = TextLinkStyles(),
            table = MaterialTheme.typography.bodyMedium
        )

        Column {
            Markdown(
                content = content,
                colors = markdownColors,
                typography = markdownTypography,
                modifier = Modifier.fillMaxWidth()
            )

            // 如果消息正在流式传输，显示打字指示器
            if (isStreaming) {
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