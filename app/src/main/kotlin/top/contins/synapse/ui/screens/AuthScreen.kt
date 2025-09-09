package top.contins.synapse.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.hilt.navigation.compose.hiltViewModel

import top.contins.synapse.R
import top.contins.synapse.viewmodel.AuthViewModel

/**
 * 认证界面
 * 提供用户登录和注册功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AuthScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var serverIp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var captchaCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 处理登录逻辑
    fun handleLogin() {
        if (serverIp.isEmpty() || email.isEmpty() || password.isEmpty() || captchaCode.isEmpty()) {
            errorMessage = "请填写所有必填项"
            return
        }
        isLoading = true
        viewModel.login(serverIp, email, password) { success, message ->
            isLoading = false
            if (success) {
                onLoginSuccess()
            } else {
                errorMessage = message ?: "登录失败"
            }
        }
    }

    // 处理注册逻辑
    fun handleRegister() {
        if (serverIp.isEmpty() || email.isEmpty() || password.isEmpty() || captchaCode.isEmpty()) {
            errorMessage = "请填写所有必填项"
            return
        }
        isLoading = true
        viewModel.register(serverIp, email, password,captchaId = "", captchaCode) { success, message ->
            isLoading = false
            if (success) {
                onLoginSuccess()
            } else {
                errorMessage = message ?: "注册失败"
            }
        }
    }


    // 使用 Scaffold + TopAppBar，使 header 固定
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 32.dp)
                    .height(128.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // 小 logo
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(12.dp)) // 圆角
                        )
                    }
                }
            )
        }, bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "© 2025 Synapse. All rights reserved.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        })
    { innerPadding ->
        // 内容区支持滚动，避免输入法遮挡
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Synapse",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "智能生活助手",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))

            // 表单 Card，保持之前布局但允许 scroll
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                // 模式切换
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (isLoginMode) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // 禁用波纹效果
                            ) { isLoginMode = true }) {
                        Text(
                            text = "登录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLoginMode) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                if (!isLoginMode) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null // 禁用波纹效果
                            ) { isLoginMode = false }
                    ) {
                        Text(
                            text = "注册",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isLoginMode) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }

                // 滑块指示器
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant
                                    .copy(alpha = 0.5f)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(
                                if (isLoginMode) Alignment.CenterStart
                                else Alignment.CenterEnd
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        value = serverIp,
                        onValueChange = { serverIp = it },
                        label = { Text("服务器地址") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Web,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("邮箱") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isLoginMode) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = captchaCode,
                                onValueChange = { captchaCode = it },
                                label = { Text("验证码") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                },
                                visualTransformation = VisualTransformation.None,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
                                modifier = Modifier.weight(1f)
                            )

                            // 判断是否已获取验证码
                            if (captchaCode.isEmpty()) {
                                // 刷新验证码按钮

                                IconButton(
                                    onClick = {
                                        // 调用获取验证码的逻辑
                                        // captchaCode = generateCaptcha()
                                    },
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .width(120.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null // 禁用波纹效果
                                        ) {
                                            // 调用获取验证码的逻辑
                                            // captchaCode = generateCaptcha()
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "刷新验证码",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "获取验证码",
                                            modifier = Modifier.padding(start = 4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                // 显示验证码的按钮
                                Text(
                                    text = captchaCode,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .width(120.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            errorMessage = ""
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "请输入邮箱和密码"
                                return@Button
                            }
                            if (!isLoginMode && password != captchaCode) {
                                errorMessage = "两次输入的密码不一致"
                                return@Button
                            }

                            isLoading = true
                            // 这里应调用真实的网络/认证逻辑；预览/本地示例中直接模拟成功
                            // 模拟结束
                            isLoading = false
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = if (isLoginMode) "登录" else "注册")
                        }
                    }


                }
            }



            if (errorMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }


        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

