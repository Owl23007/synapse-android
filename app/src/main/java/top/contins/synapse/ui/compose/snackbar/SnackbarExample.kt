package top.contins.synapse.ui.compose.snackbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 演示如何在 Compose 中使用不同级别 SnackbarController 的示例组件
 */
@Composable
fun SnackbarExample() {
    val controller = SnackbarController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 成功消息按钮
        Button(
            onClick = {
                controller.showSuccessMessage("操作成功完成！")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("显示成功消息")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 信息消息按钮
        Button(
            onClick = {
                controller.showInfoMessage(
                    message = "这是一条信息消息",
                    action = SnackbarAction(
                        title = "了解更多",
                        onActionPress = {
                            controller.showInfoMessage("您点击了了解更多")
                        }
                    )
                )
            }
        ) {
            Text("显示信息消息")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 警告消息按钮
        Button(
            onClick = {
                controller.showWarningMessage(
                    message = "请注意：存储空间不足",
                    action = SnackbarAction(
                        title = "清理",
                        onActionPress = {
                            controller.showSuccessMessage("清理完成")
                        }
                    ),
                    duration = SnackbarDuration.Long
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
        ) {
            Text("显示警告消息")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 错误消息按钮
        Button(
            onClick = {
                controller.showErrorMessage(
                    message = "网络连接失败",
                    action = SnackbarAction(
                        title = "重试",
                        onActionPress = {
                            controller.showInfoMessage("正在重新连接...")
                        }
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
        ) {
            Text("显示错误消息")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 从静态方法调用示例
        Button(
            onClick = {
                // 演示从非 Compose 代码中显示不同级别的 Snackbar
                SnackbarController.showErrorMessage(
                    message = "从静态方法调用的错误消息",
                    action = SnackbarAction(
                        title = "修复",
                        onActionPress = {
                            SnackbarController.showSuccessMessage("问题已修复")
                        }
                    )
                )
            }
        ) {
            Text("静态方法调用示例")
        }
    }
}
