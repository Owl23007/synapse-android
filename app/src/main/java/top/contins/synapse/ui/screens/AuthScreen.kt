package top.contins.synapse.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.R
import top.contins.synapse.domain.model.AuthUiState
import top.contins.synapse.viewmodel.AuthViewModel
import kotlin.io.encoding.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AuthScreen(
    onLoginSuccess: () -> Unit = {}, viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var serverIp by remember { mutableStateOf("http://192.168.99.10:8081") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var captchaCode by remember { mutableStateOf("") }
    var captchaId by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }
    var isRefreshingCaptcha by remember { mutableStateOf(false) }
    var captchaImageBase64 by remember { mutableStateOf("") }

    fun isValidServerUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val uri = url.toUri()
            uri.scheme in listOf("http", "https") &&
                    !uri.host.isNullOrEmpty() &&
                    uri.host?.contains('.') == true
        } catch (_: Exception) {
            false
        }
    }

    val handleLogin = {
        if (isValidServerUrl(serverIp) && email.isNotBlank() && password.isNotBlank()) {
            viewModel.login(email, password, serverIp)
        }
    }

    val handleRegister = {
        if (isValidServerUrl(serverIp) && email.isNotBlank() && password.isNotBlank() &&
            captchaCode.isNotBlank() && captchaId.isNotBlank()
        ) {
            viewModel.register(email, username, password, captchaId, captchaCode, serverIp)
        }
    }

    val handleRefreshCaptcha = {
        if (!isRefreshingCaptcha && isValidServerUrl(serverIp)) {
            isRefreshingCaptcha = true
            viewModel.getCaptcha(serverIp)
        }
    }

    LaunchedEffect(uiState) {
        when (val currentState = uiState) {
            is AuthUiState.CaptchaLoaded -> {
                captchaId = currentState.response.captchaId
                captchaImageBase64 = currentState.response.captchaImageBase64
                isRefreshingCaptcha = false
            }

            is AuthUiState.CaptchaError -> {
                isRefreshingCaptcha = false
                captchaImageBase64 = "" // 清空图片
            }

            is AuthUiState.RegisterSuccess -> {

            }

            is AuthUiState.LoginSuccess -> {
                onLoginSuccess()
            }

            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 32.dp)
                    .height(128.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp),
                containerColor = MaterialTheme.colorScheme.background,
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                Text(
                    text = "© 2025 Synapse. All rights reserved.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    ) { innerPadding ->
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

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ModeButton(
                            text = "登录",
                            isSelected = isLoginMode,
                            onClick = { isLoginMode = true })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ModeButton(
                            text = "注册",
                            isSelected = !isLoginMode,
                            onClick = { isLoginMode = false })
                    }
                }

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
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(if (isLoginMode) Alignment.CenterStart else Alignment.CenterEnd)
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
                        leadingIcon = { Icon(Icons.Default.Web, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("邮箱") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("用户名") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Unspecified),
                                modifier = Modifier.weight(0.5f)
                            )

                            val isLoadingCaptcha = uiState is AuthUiState.Loading


                            Box(
                                modifier =
                                    Modifier
                                        .weight(0.4f)
                                        .padding(start= 10.dp)
                                        .height(64.dp)
                                        .padding(top= 6.dp,bottom= 0.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (!isLoadingCaptcha && !isRefreshingCaptcha && isValidServerUrl(
                                                    serverIp
                                                )
                                            ) {
                                                handleRefreshCaptcha()
                                            }
                                        }
                                        .padding(4.dp), // 内边距让图片不贴边
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingCaptcha || isRefreshingCaptcha) {
                                    // 加载中：显示进度条
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else if (captchaImageBase64.isNotEmpty()) {
                                    // 正常显示验证码图片
                                    Base64Image(
                                        base64String = captchaImageBase64,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp) // 防止图片贴边
                                    )
                                } else {
                                    // 初始/错误状态：显示提示文字或占位图
                                    Text(
                                        text = "点击加载验证码",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val buttonText = if (isLoginMode) "登录" else "注册"
                    val isActionEnabled = isValidServerUrl(serverIp) &&
                            email.isNotBlank() &&
                            password.isNotBlank() &&
                            (isLoginMode || (captchaCode.isNotBlank() && captchaId.isNotBlank()))

                    Button(
                        onClick = if (isLoginMode) handleLogin else handleRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = isActionEnabled && uiState !is AuthUiState.Loading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = buttonText)
                        }
                    }

                    if (uiState is AuthUiState.LoginError || uiState is AuthUiState.RegisterError || uiState is AuthUiState.CaptchaError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val errorMessage = when (val currentState = uiState) {
                            is AuthUiState.LoginError -> currentState.message
                            is AuthUiState.RegisterError -> currentState.message
                            is AuthUiState.CaptchaError -> currentState.message
                            else -> ""
                        }

                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.resetAuthState()
                                    captchaCode = ""
                                    captchaId = ""
                                }
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModeButton(
    text: String, isSelected: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun Base64Image(
    base64String: String,
    modifier: Modifier = Modifier
) {
    val imageBitmap = base64ToBitmap(base64String)?.asImageBitmap()

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Captcha Image",
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        // 显示错误占位符
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "加载失败",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp
            )
        }
    }
}

fun base64ToBitmap(base64String: String): Bitmap? {
    Log.d("AuthScreen", "base64ToBitmap: $base64String")
    return try {
        // 移除 Data URL 前缀（支持常见格式）
        val cleaned = base64String
            .removePrefix("data:image/png;base64,")
            .removePrefix("data:image/jpeg;base64,")
            .removePrefix("data:image/jpg;base64,")
            .removePrefix("data:image/gif;base64,")
            .removePrefix("data:image/webp;base64,")

        // 解码 Base64
        val decodedBytes = Base64.decode(cleaned)

        // 转为 Bitmap
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
