package top.contins.synapse.ui.compose.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * SnackbarController 提供者组件
 * 通过 CompositionLocal 在整个 Compose 树中提供 SnackbarController 实例
 * 支持不同级别的 Snackbar 显示
 *
 * @param content 接收 SnackbarHostState 和当前级别作为参数的 Composable 内容
 */
@Composable
fun SnackbarControllerProvider(content: @Composable (snackbarHost: SnackbarHostState, currentLevel: SnackbarLevel) -> Unit) {
    val snackHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackController = remember(scope) {
        SnackbarController(snackHostState, scope)
    }

    // 获取当前 Snackbar 级别状态
    val currentLevel by currentSnackbarLevel

    // 监听来自外部 Channel 的消息
    DisposableEffect(snackController, scope) {
        val job = scope.launch {
            for (payload in snackbarChannel) {
                snackController.showMessage(
                    message = payload.message,
                    duration = payload.duration,
                    action = payload.action,
                    level = payload.level
                )
            }
        }

        onDispose {
            job.cancel()
        }
    }

    CompositionLocalProvider(LocalSnackbarController provides snackController) {
        content(snackHostState, currentLevel)
    }
}
