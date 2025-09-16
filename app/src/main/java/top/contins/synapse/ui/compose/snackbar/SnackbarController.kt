package top.contins.synapse.ui.compose.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 全局 Channel，用于在 Compose 上下文外部发送 Snackbar 消息
 */
val snackbarChannel = Channel<SnackbarChannelMessage>(capacity = Int.MAX_VALUE)

/**
 * 全局状态，用于跟踪当前 Snackbar 的级别
 */
val currentSnackbarLevel = mutableStateOf(SnackbarLevel.INFO)

/**
 * CompositionLocal，用于在 Compose 树中访问 SnackbarController
 */
val LocalSnackbarController = staticCompositionLocalOf {
    SnackbarController(
        host = SnackbarHostState(),
        scope = CoroutineScope(EmptyCoroutineContext)
    )
}

/**
 * Snackbar 控制器
 * 负责管理 Snackbar 的显示、隐藏和关闭，支持不同级别的颜色主题
 */
@Immutable
class SnackbarController(
    private val host: SnackbarHostState,
    private val scope: CoroutineScope,
) {

    companion object {
        /**
         * 获取当前 Compose 上下文中的 SnackbarController
         */
        val current
            @Composable
            @ReadOnlyComposable
            get() = LocalSnackbarController.current

        /**
         * 从 Compose 上下文外部显示成功消息
         */
        fun showSuccessMessage(
            message: String,
            action: SnackbarAction? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
        ) {
            showMessage(message, action, duration, SnackbarLevel.SUCCESS)
        }

        /**
         * 从 Compose 上下文外部显示信息消息
         */
        fun showInfoMessage(
            message: String,
            action: SnackbarAction? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
        ) {
            showMessage(message, action, duration, SnackbarLevel.INFO)
        }

        /**
         * 从 Compose 上下文外部显示警告消息
         */
        fun showWarningMessage(
            message: String,
            action: SnackbarAction? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
        ) {
            showMessage(message, action, duration, SnackbarLevel.WARNING)
        }

        /**
         * 从 Compose 上上下文外部显示错误消息
         */
        fun showErrorMessage(
            message: String,
            action: SnackbarAction? = null,
            duration: SnackbarDuration = SnackbarDuration.Long,
        ) {
            showMessage(message, action, duration, SnackbarLevel.ERROR)
        }

        /**
         * 从 Compose 上下文外部显示 Snackbar
         * 适用于在 ViewModel 或其他非 Compose 代码中使用
         */
        fun showMessage(
            message: String,
            action: SnackbarAction? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
            level: SnackbarLevel = SnackbarLevel.INFO,
        ) {
            snackbarChannel.trySend(
                SnackbarChannelMessage(
                    message = message,
                    action = action,
                    duration = duration,
                    level = level
                )
            )
        }
    }

    /**
     * 在 Compose 上下文中显示成功消息
     */
    fun showSuccessMessage(
        message: String,
        action: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        showMessage(message, action, duration, SnackbarLevel.SUCCESS)
    }

    /**
     * 在 Compose 上下文中显示信息消息
     */
    fun showInfoMessage(
        message: String,
        action: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        showMessage(message, action, duration, SnackbarLevel.INFO)
    }

    /**
     * 在 Compose 上下文中显示警告消息
     */
    fun showWarningMessage(
        message: String,
        action: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        showMessage(message, action, duration, SnackbarLevel.WARNING)
    }

    /**
     * 在 Compose 上下文中显示错误消息
     */
    fun showErrorMessage(
        message: String,
        action: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Long,
    ) {
        showMessage(message, action, duration, SnackbarLevel.ERROR)
    }

    /**
     * 在 Compose 上下文中显示 Snackbar
     * @param message 要显示的消息
     * @param action 可选的操作按钮
     * @param duration 显示时长
     * @param level 显示级别，用于确定颜色主题
     */
    fun showMessage(
        message: String,
        action: SnackbarAction? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        level: SnackbarLevel = SnackbarLevel.INFO,
    ) {
        scope.launch {
            // 更新当前 Snackbar 的级别
            currentSnackbarLevel.value = level

            /**
             * 取消注释下面这行代码，如果你希望 Snackbar 立即显示，
             * 而不是排队等待当前显示的 Snackbar 消失
             */
            host.currentSnackbarData?.dismiss()

            val result = host.showSnackbar(
                message = message,
                actionLabel = action?.title,
                duration = duration
            )

            if (result == SnackbarResult.ActionPerformed) {
                action?.onActionPress?.invoke()
            }
        }
    }
}
