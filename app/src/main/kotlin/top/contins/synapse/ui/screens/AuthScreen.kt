package top.contins.synapse.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 认证屏幕
 * 提供用户登录和注册功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo 区域
        Card(
            modifier = Modifier.size(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 标题
        Text(
            text = "Synapse",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "智能助手平台",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 模式切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { isLoginMode = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isLoginMode) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "登录",
                    fontWeight = if (isLoginMode) FontWeight.Bold else FontWeight.Normal
                )
            }

            Text(
                text = " | ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            TextButton(
                onClick = { isLoginMode = false },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (!isLoginMode) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = "注册",
                    fontWeight = if (!isLoginMode) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 表单
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 邮箱输入
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    label = { Text("邮箱") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = "邮箱")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 密码输入
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text("密码") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "密码")
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                             else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                         else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 确认密码（仅注册模式）
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        label = { Text("确认密码") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "确认密码")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 错误消息
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 主要操作按钮
                Button(
                    onClick = {
                        // 表单验证
                        when {
                            email.isEmpty() -> errorMessage = "请输入邮箱"
                            password.isEmpty() -> errorMessage = "请输入密码"
                            !isLoginMode && confirmPassword.isEmpty() -> errorMessage = "请确认密码"
                            !isLoginMode && password != confirmPassword -> errorMessage = "两次输入的密码不一致"
                            !email.contains("@") -> errorMessage = "请输入有效的邮箱地址"
                            password.length < 6 -> errorMessage = "密码长度至少6位"
                            else -> {
                                // 模拟登录/注册过程
                                isLoading = true
                                // 这里可以添加实际的认证逻辑
                                // 暂时直接成功
                                onLoginSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isLoginMode) "登录" else "注册")
                    }
                }

                // 快速登录（仅登录模式）
                if (isLoginMode) {
                    OutlinedButton(
                        onClick = {
                            // 快速登录，跳过验证
                            onLoginSuccess()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("快速体验")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 忘记密码（仅登录模式）
        if (isLoginMode) {
            TextButton(
                onClick = {
                    // TODO: 实现忘记密码功能
                }
            ) {
                Text(
                    text = "忘记密码？",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
