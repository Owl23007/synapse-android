package top.contins.synapse.core.ui.compose.snackbar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

/**
 * 自定义 Snackbar 组件，支持不同级别的颜色主题
 * @param snackbarData Snackbar 数据
 * @param level Snackbar 显示级别
 * @param modifier 修饰符
 */
@Composable
fun CustomSnackbar(
    modifier: Modifier = Modifier,
    snackbarData: SnackbarData,
    level: SnackbarLevel = SnackbarLevel.INFO,
) {
    val containerColor = level.getContainerColor()
    val contentColor = level.getContentColor()

    // 获取屏幕高度并计算底部边距 - 使用推荐的现代方法
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val screenHeightPx = windowInfo.containerSize.height
    val screenHeightDp = with(density) { screenHeightPx.toDp() }
    val bottomPadding = screenHeightDp - 220.dp

    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 42.dp, end = 42.dp, bottom = bottomPadding)
            .widthIn(max = 500.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = contentColor,
        dismissActionContentColor = contentColor
    )

}

/**
 * 带级别的 SnackbarHost 组件
 * @param hostState SnackbarHostState
 * @param level 当前 Snackbar 的级别
 * @param modifier 修饰符
 */
@Composable
fun LevelSnackbarHost(
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState,
    level: SnackbarLevel = SnackbarLevel.INFO,

) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        CustomSnackbar(
            snackbarData = snackbarData,
            level = level
        )
    }
}
