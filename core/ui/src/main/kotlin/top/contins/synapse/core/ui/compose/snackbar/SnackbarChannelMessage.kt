package top.contins.synapse.core.ui.compose.snackbar

import androidx.compose.material3.SnackbarDuration

/**
 * 用于通过 Channel 传递的 Snackbar 消息数据类
 * @param message 要显示的消息文本
 * @param action 可选的操作按钮
 * @param duration Snackbar 显示时长
 * @param level Snackbar 显示级别，用于确定颜色主题
 */
data class SnackbarChannelMessage(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val level: SnackbarLevel = SnackbarLevel.INFO,
)
