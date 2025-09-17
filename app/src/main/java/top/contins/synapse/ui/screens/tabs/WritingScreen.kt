package top.contins.synapse.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.data.model.WritingContent
import top.contins.synapse.ui.screens.WritingEditorScreen
import top.contins.synapse.ui.viewmodel.WritingViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 写作页面 - 创作中心
 * 未来整合日程/待办功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(
    viewModel: WritingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drafts by viewModel.drafts.collectAsState()
    val published by viewModel.published.collectAsState()
    
    val tabs = listOf("创作", "草稿", "已发布")

    // 显示消息
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // 可以在这里显示Toast或Snackbar
            kotlinx.coroutines.delay(100) // 小延迟确保消息显示
        }
    }

    if (uiState.showEditor) {
        WritingEditorScreen(
            initialContent = uiState.currentEditingContent ?: WritingContent(),
            onSave = { content ->
                viewModel.saveWriting(content)
            },
            onPublish = { content ->
                viewModel.publishWriting(content)
            },
            onBack = {
                viewModel.hideEditor()
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tab栏
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            when (uiState.selectedTab) {
                0 -> CreateTab(
                    onStartWriting = { template ->
                        viewModel.showEditor(template = template)
                    }
                )
                1 -> DraftTab(
                    drafts = drafts,
                    onEditDraft = { draft ->
                        viewModel.showEditor(content = draft)
                    },
                    onDeleteDraft = { draft ->
                        viewModel.deleteWriting(draft)
                    }
                )
                2 -> PublishedTab(
                    published = published,
                    onEditPublished = { content ->
                        viewModel.showEditor(content = content)
                    }
                )
            }
        }
        
        // 显示消息的Snackbar
        uiState.message?.let { message ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun CreateTab(onStartWriting: (String?) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 快速创作按钮
        ElevatedButton(
            onClick = { onStartWriting(null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始新的创作", fontSize = 16.sp,color = Color.White)
        }

        // 创作模板
        Text(
            text = "创作模板",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getWritingTemplates()) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onStartWriting(template.title) }
                )
            }
        }
    }
}

@Composable
fun DraftTab(
    drafts: List<WritingContent>,
    onEditDraft: (WritingContent) -> Unit = {},
    onDeleteDraft: (WritingContent) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteDraft by remember { mutableStateOf<WritingContent?>(null) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (drafts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无草稿",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击\"开始新的创作\"来创建您的第一篇文章",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(drafts) { draft ->
                DraftCard(
                    draft = draft,
                    onEdit = { onEditDraft(draft) },
                    onDelete = { 
                        pendingDeleteDraft = draft
                        showDeleteDialog = true
                    }
                )
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog && pendingDeleteDraft != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                pendingDeleteDraft = null
            },
            title = { Text("确认删除") },
            text = { 
                Text("确定要删除草稿「${pendingDeleteDraft?.title?.ifEmpty { "无标题" }}」吗？此操作无法撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteDraft?.let { onDeleteDraft(it) }
                        showDeleteDialog = false
                        pendingDeleteDraft = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        pendingDeleteDraft = null
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun PublishedTab(
    published: List<WritingContent>,
    onEditPublished: (WritingContent) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (published.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Publish,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无已发布的作品",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            items(published) { item ->
                PublishedCard(
                    item = item,
                    onEdit = { onEditPublished(item) }
                )
            }
        }
    }
}

data class WritingTemplate(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getWritingTemplates() = listOf(
    WritingTemplate("日记", "记录每日心情和感悟", Icons.Default.Book),
    WritingTemplate("工作总结", "专业的工作总结模板", Icons.Default.Work),
    WritingTemplate("学习笔记", "整理学习内容和知识点", Icons.Default.School),
    WritingTemplate("项目计划", "制定详细的项目执行计划", Icons.Default.Assignment),
    WritingTemplate("创意文案", "营销和宣传文案创作", Icons.Default.Lightbulb)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(
    template: WritingTemplate,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                template.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = template.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftCard(
    draft: WritingContent,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (draft.title.isEmpty()) "无标题" else draft.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (draft.content.isEmpty()) "暂无内容" else draft.content.take(100) + if (draft.content.length > 100) "..." else "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatTime(draft.updatedAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> {
            val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedCard(
    item: WritingContent,
    onEdit: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.title.isEmpty()) "无标题" else item.title,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "已发布",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("查看") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = {
                                showMenu = false
                                // TODO: 实现分享功能
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (item.content.isEmpty()) "暂无内容" else item.content.take(100) + if (item.content.length > 100) "..." else "",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(item.updatedAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = "👍 ${(0..50).random()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💬 ${(0..20).random()}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WritingScreenPreview() {
    // Preview函数不能使用依赖注入，所以这里暂时留空或使用模拟数据
    // WritingScreen()
}