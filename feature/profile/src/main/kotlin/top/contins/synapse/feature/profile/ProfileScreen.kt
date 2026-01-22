package top.contins.synapse.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.contins.synapse.domain.model.auth.User
import coil.compose.AsyncImage


/**
 * Profile 页面 - 专注于日程管理
 * 
 * 主要功能：
 * - 日程导入/导出功能
 * - 日历订阅管理
 * - 用户资料信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scheduleAction by viewModel.scheduleAction.collectAsStateWithLifecycle()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    
    // 监听登出状态
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.LoggedOut) {
            onLogout()
            viewModel.resetState()
        }
    }
    
    // 处理日程操作结果
    LaunchedEffect(scheduleAction) {
        // 如需要可处理不同的操作状态
    }

    val user = (uiState as? ProfileUiState.Success)?.user
    val subscriptions = (uiState as? ProfileUiState.Success)?.subscriptions ?: emptyList()

    if (uiState is ProfileUiState.Loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState is ProfileUiState.Error) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("加载失败: ${(uiState as ProfileUiState.Error).message}")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.loadUserProfile() }) {
                    Text("重试")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // 用户信息卡片
                UserProfileCard(user = user)
            }
            
            item {
                // 日程管理区域
                Text(
                    text = "日程管理",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            item {
                ScheduleManagementCard(
                    onImportExport = { showImportExportDialog = true },
                    onManageSubscriptions = { showSubscriptionDialog = true }
                )
            }
            
            item {
                // 订阅列表
                Text(
                    text = "日历订阅 (${subscriptions.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            items(subscriptions) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onSync = { 
                        viewModel.syncSubscription(subscription.id, subscription.name) 
                    },
                    onDelete = { 
                        viewModel.deleteSubscription(subscription.id) 
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // 设置区域
                Text(
                    text = "设置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            items(getSettingsMenuItems()) { menuItem ->
                ProfileMenuItem(
                    menuItem = menuItem,
                    onClick = {
                        if (menuItem.title == "退出登录") {
                            showLogoutDialog = true
                        }
                    }
                )
            }
        }
    }
    
    // 登出确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    
    // 导入/导出对话框
    if (showImportExportDialog) {
        ImportExportDialog(
            onDismiss = { showImportExportDialog = false },
            viewModel = viewModel
        )
    }
    
    // 订阅管理对话框
    if (showSubscriptionDialog) {
        SubscriptionManagementDialog(
            onDismiss = { showSubscriptionDialog = false },
            viewModel = viewModel
        )
    }
    
    // 登出加载状态
    if (uiState is ProfileUiState.LoggingOut) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在退出...")
            }
        }
    }
}

/**
 * 用户资料卡片
 */
@Composable
fun UserProfileCard(user: User? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            if (user?.avatar.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            } else {
                AsyncImage(
                    model = user!!.avatar,
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.nickname ?: "Synapse用户",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = user?.signature?.ifEmpty { "日程管理助手" } ?: "日程管理助手",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 日程管理卡片
 */
@Composable
fun ScheduleManagementCard(
    onImportExport: () -> Unit,
    onManageSubscriptions: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "日程工具",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScheduleActionButton(
                    icon = Icons.Default.ImportExport,
                    label = "导入导出",
                    onClick = onImportExport
                )
                ScheduleActionButton(
                    icon = Icons.Default.Subscriptions,
                    label = "订阅管理",
                    onClick = onManageSubscriptions
                )
            }
        }
    }
}

/**
 * 日程操作按钮
 */
@Composable
fun ScheduleActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp)
        }
    }
}

/**
 * 订阅卡片
 */
@Composable
fun SubscriptionCard(
    subscription: top.contins.synapse.domain.model.schedule.Subscription,
    onSync: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Subscriptions,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subscription.url,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                subscription.lastSyncAt?.let {
                    Text(
                        text = "最后同步: ${remember(it) { 
                            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(it))
                        }}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onSync) {
                Icon(Icons.Default.Sync, contentDescription = "同步")
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

/**
 * Profile 菜单项数据类
 */
data class ProfileMenuItem(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val showChevron: Boolean = true
)

/**
 * 获取设置菜单项列表
 */
fun getSettingsMenuItems() = listOf(
    ProfileMenuItem(
        title = "通知设置",
        subtitle = "管理推送和提醒",
        icon = Icons.Default.Notifications
    ),
    ProfileMenuItem(
        title = "数据备份",
        subtitle = "云端同步和备份",
        icon = Icons.Default.CloudSync
    ),
    ProfileMenuItem(
        title = "帮助中心",
        subtitle = "使用指南和常见问题",
        icon = Icons.Default.Help
    ),
    ProfileMenuItem(
        title = "关于我们",
        subtitle = "版本信息和团队介绍",
        icon = Icons.Default.Info
    ),
    ProfileMenuItem(
        title = "退出登录",
        subtitle = "退出当前账号",
        icon = Icons.Default.ExitToApp,
        showChevron = false
    )
)

/**
 * Profile 菜单项组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuItem(
    menuItem: ProfileMenuItem,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    menuItem.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.title,
                    fontWeight = FontWeight.Medium
                )
                
                if (menuItem.subtitle.isNotEmpty()) {
                    Text(
                        text = menuItem.subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 右箭头
            if (menuItem.showChevron) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 导入/导出对话框
 */
@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入导出") },
        text = {
            Column {
                Text("日程导入导出功能")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 支持iCalendar (.ics)格式", fontSize = 12.sp)
                Text("• 可从文件导入日程", fontSize = 12.sp)
                Text("• 可导出日程到文件", fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 订阅管理对话框
 */
@Composable
fun SubscriptionManagementDialog(
    onDismiss: () -> Unit,
    viewModel: ProfileViewModel
) {
    var subscriptionName by remember { mutableStateOf("") }
    var subscriptionUrl by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加订阅") },
        text = {
            Column {
                OutlinedTextField(
                    value = subscriptionName,
                    onValueChange = { subscriptionName = it },
                    label = { Text("订阅名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subscriptionUrl,
                    onValueChange = { subscriptionUrl = it },
                    label = { Text("订阅URL") },
                    placeholder = { Text("https://example.com/calendar.ics") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (subscriptionName.isNotBlank() && subscriptionUrl.isNotBlank()) {
                        viewModel.createSubscription(
                            name = subscriptionName,
                            url = subscriptionUrl
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
