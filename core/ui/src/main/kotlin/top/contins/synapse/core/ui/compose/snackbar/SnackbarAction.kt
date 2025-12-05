package top.contins.synapse.core.ui.compose.snackbar

/**
 * Snackbar 操作数据类
 * @param title 操作按钮显示的文本
 * @param onActionPress 点击操作按钮时执行的回调
 * @param level Snackbar 显示级别，用于确定颜色主题
 */
data class SnackbarAction(
    val title: String,
    val onActionPress: () -> Unit,
    val level: SnackbarLevel = SnackbarLevel.INFO
)
