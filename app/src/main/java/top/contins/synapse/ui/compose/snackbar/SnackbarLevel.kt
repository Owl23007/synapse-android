package top.contins.synapse.ui.compose.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Snackbar 显示级别枚举
 * 定义不同类型的消息级别及其对应的颜色
 */
enum class SnackbarLevel {
    SUCCESS,    // 成功消息 - 绿色
    INFO,       // 信息消息 - 蓝色
    WARNING,    // 警告消息 - 橙色
    ERROR;      // 错误消息 - 红色

    /**
     * 获取该级别对应的容器颜色
     */
    @Composable
    fun getContainerColor(): Color {
        return when (this) {
            SUCCESS -> Color(0xC74CAF50)  // 绿色
            INFO ->  Color(0xC600BCD4)
            WARNING -> Color(0xC7FF9800)  // 橙色
            ERROR -> Color(0xC7E53935)
        }
    }

    /**
     * 获取该级别对应的内容颜色（文字颜色）
     */
    @Composable
    fun getContentColor(): Color {
        return when (this) {
            SUCCESS -> Color(0xFFFFFFFF)  // 白色
            INFO -> MaterialTheme.colorScheme.onPrimary  // 主题色上的文字
            WARNING -> Color(0xFFFFFFFF)  // 白色
            ERROR -> MaterialTheme.colorScheme.onError  // 错误色上的文字
        }
    }
}
