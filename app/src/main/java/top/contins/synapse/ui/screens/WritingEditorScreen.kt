package top.contins.synapse.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import top.contins.synapse.ui.viewmodel.WritingViewModel
import top.contins.synapse.data.model.WritingContent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingEditorScreen(
    initialContent: WritingContent = WritingContent(),
    onSave: (WritingContent) -> Unit = {},
    onPublish: (WritingContent) -> Unit = {},
    onBack: () -> Unit = {},
    writingViewModel: WritingViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf(initialContent.title) }
    var content by remember { mutableStateOf(initialContent.content) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    var showPublishDialog by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    
    // AI评判相关状态
    var showAiEvaluationDialog by remember { mutableStateOf(false) }
    var isEvaluating by remember { mutableStateOf(false) }
    var evaluationResult by remember { mutableStateOf("") }
    var startEvaluation by remember { mutableStateOf(false) }

    // AI评判逻辑
    LaunchedEffect(startEvaluation) {
        if (startEvaluation) {
            isEvaluating = true
            try {
                val result = writingViewModel.evaluateArticle(title, content)
                Log.d("WritingEditorScreen", "AI evaluation result received: ${result.take(100)}...")
                evaluationResult = result
            } catch (e: Exception) {
                Log.e("WritingEditorScreen", "Error during AI evaluation", e)
                evaluationResult = when (e) {
                    is java.util.concurrent.CancellationException -> {
                        // 协程被取消，不显示错误
                        ""
                    }
                    else -> "AI评判过程中出现错误：${e.message ?: "未知错误"}"
                }
            } finally {
                isEvaluating = false
                startEvaluation = false
            }
        }
    }

    // 检测是否有未保存的更改
    LaunchedEffect(title, content) {
        hasUnsavedChanges = title != initialContent.title || content != initialContent.content
    }

    // 自动保存逻辑
    LaunchedEffect(title, content) {
        if (title.isNotEmpty() || content.isNotEmpty()) {
            delay(3000) // 3秒后自动保存
            isSaving = true
            val updatedContent = initialContent.copy(
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            onSave(updatedContent)
            saveMessage = "已自动保存"
            hasUnsavedChanges = false
            delay(2000)
            saveMessage = ""
            isSaving = false
        }
    }

    // 处理返回逻辑
    val handleBack = {
        if (hasUnsavedChanges) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    // AI评判逻辑
    val handleAiEvaluation = {
        if (title.isNotEmpty() && content.isNotEmpty()) {
            showAiEvaluationDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // 标题输入
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) {
                            Text(
                                text = if (initialContent.template != null) 
                                    "请输入${initialContent.template}标题..." 
                                else 
                                    "请输入标题...",
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内容输入
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp), // 给内容区域一个固定高度
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text(
                                text = getContentPlaceholder(initialContent.template),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 底部信息栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "字数：${content.length}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (content.isNotEmpty()) {
                        val words = content.split("\\s+".toRegex()).size
                        Text(
                            text = "词数：$words",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "最后编辑：${formatTime(System.currentTimeMillis())}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // 为底部工具栏留出空间
        }

        // 浮动工具栏 - 显示在右下角
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                IconButton(onClick = handleBack) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // 保存状态指示器
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (saveMessage.isNotEmpty()) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (hasUnsavedChanges) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = "有未保存的更改",
                        modifier = Modifier.size(8.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                
                // 手动保存按钮
                IconButton(
                    onClick = {
                        val updatedContent = initialContent.copy(
                            title = title,
                            content = content,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(updatedContent)
                        hasUnsavedChanges = false
                    }
                ) {
                    Icon(
                        Icons.Default.Save, 
                        contentDescription = "保存",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // AI评判按钮
                IconButton(
                    onClick = handleAiEvaluation,
                    enabled = title.isNotEmpty() && content.isNotEmpty() && !isEvaluating
                ) {
                    if (isEvaluating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            Icons.Default.Psychology, 
                            contentDescription = "AI评判",
                            tint = if (title.isNotEmpty() && content.isNotEmpty()) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // 发布按钮
                IconButton(
                    onClick = { showPublishDialog = true },
                    enabled = title.isNotEmpty() && content.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.Publish, 
                        contentDescription = "发布",
                        tint = if (title.isNotEmpty() && content.isNotEmpty()) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // 模板信息提示 - 显示在左上角
        if (initialContent.template != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = "模板：${initialContent.template}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // 保存状态消息 - 显示在顶部中央
        if (saveMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = saveMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
    
    // 发布确认对话框
    if (showPublishDialog) {
        AlertDialog(
            onDismissRequest = { showPublishDialog = false },
            title = { Text("发布文章") },
            text = { 
                Column {
                    Text("确定要发布这篇文章吗？发布后其他用户将能够看到您的作品。")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "标题：${title.ifEmpty { "无标题" }}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "字数：${content.length}字",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val publishedContent = initialContent.copy(
                            title = title,
                            content = content,
                            isPublished = true,
                            updatedAt = System.currentTimeMillis()
                        )
                        onPublish(publishedContent)
                        showPublishDialog = false
                    }
                ) {
                    Text("发布")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPublishDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("有未保存的更改") },
            text = { 
                Text("您有未保存的更改，确定要退出吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    }
                ) {
                    Text("退出", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // AI评判对话框
    if (showAiEvaluationDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isEvaluating) {
                    showAiEvaluationDialog = false
                    evaluationResult = ""
                    startEvaluation = false
                }
            },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("AI文章评判")
                }
            },
            text = { 
                Column {
                    Text("将会分析您的文章并提供评价和建议。")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isEvaluating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("AI正在分析您的文章...")
                        }
                    } else if (evaluationResult.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "评判结果：",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 可滚动的评判结果
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = evaluationResult,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(rememberScrollState()),
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        Text(
                            text = "标题：${title.ifEmpty { "无标题" }}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "字数：${content.length}字",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (!isEvaluating) {
                    if (evaluationResult.isEmpty()) {
                        TextButton(
                            onClick = {
                                startEvaluation = true
                            }
                        ) {
                            Text("开始评判")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                showAiEvaluationDialog = false
                                evaluationResult = ""
                                startEvaluation = false
                            }
                        ) {
                            Text("关闭")
                        }
                    }
                }
            },
            dismissButton = {
                if (!isEvaluating) {
                    TextButton(
                        onClick = { 
                            showAiEvaluationDialog = false
                            evaluationResult = ""
                            startEvaluation = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

fun getContentPlaceholder(template: String?): String {
    return when (template) {
        "日记" -> "今天发生了什么有趣的事情？\n记录您的心情和感悟...\n\n例如：\n- 今天的天气如何？\n- 遇到了什么人或事？\n- 有什么新的感悟或想法？"
        "工作总结" -> "本周/月工作总结\n\n完成的主要工作：\n1. \n2. \n3. \n\n遇到的问题及解决方案：\n\n下一步计划：\n\n个人收获与感悟："
        "学习笔记" -> "学习内容概述\n\n重点知识点：\n1. \n2. \n3. \n\n个人理解：\n\n疑问和思考：\n\n实践应用："
        "项目计划" -> "项目背景\n\n项目目标：\n\n主要任务：\n1. \n2. \n3. \n\n时间安排：\n\n预期成果：\n\n风险评估："
        "创意文案" -> "文案主题：\n\n目标受众：\n\n核心信息：\n\n创意亮点：\n\n行动召唤："
        else -> "开始您的创作...\n\n在这里尽情发挥您的想象力，记录您的思考和感悟。\n\n您可以写下：\n• 今天的所见所闻\n• 内心的感悟与思考\n• 学习的心得体会\n• 工作的总结反思\n• 生活的点滴记录"
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun WritingEditorScreenPreview() {
    WritingEditorScreen(
        initialContent = WritingContent(
            title = "示例标题",
            content = "这是一些示例内容..."
        )
    )
}