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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import top.contins.synapse.domain.model.User
import coil.compose.AsyncImage


/**
 * 我的页面 - 个人资料、作品、设置、会员
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // 监听登出状态
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.LoggedOut) {
            onLogout()
            viewModel.resetState()
        }
    }

    val user = (uiState as? ProfileUiState.Success)?.user

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
                // 数据统计
                UserStatsCard()
            }
            
            item {
                // 功能菜单
                Text(
                    text = "功能",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            items(getProfileMenuItems()) { menuItem ->
                ProfileMenuItem(menuItem = menuItem)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // 设置菜单
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
                    contentDescription = "Avatar",
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
                    text = user?.signature?.ifEmpty { "AI写作助手的忠实用户" } ?: "AI写作助手的忠实用户",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "🎯 高级会员",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "编辑资料",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun UserStatsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "数据统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("创作", "15", "篇文章")
                StatItem("点赞", "128", "次获赞")
                StatItem("关注", "56", "位朋友")
                StatItem("等级", "LV.8", "创作者")
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String, suffix: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = suffix,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ProfileMenuItem(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val showBadge: Boolean = false,
    val badgeText: String = "",
    val showChevron: Boolean = true
)

fun getProfileMenuItems() = listOf(
    ProfileMenuItem(
        title = "我的作品",
        subtitle = "查看已发布的文章和草稿",
        icon = Icons.Default.Article
    ),
    ProfileMenuItem(
        title = "收藏夹",
        subtitle = "收藏的优质内容",
        icon = Icons.Default.Bookmark
    ),
    ProfileMenuItem(
        title = "学习记录",
        subtitle = "AI学习进度和成就",
        icon = Icons.Default.School
    ),
    ProfileMenuItem(
        title = "会员中心",
        subtitle = "查看会员权益和续费",
        icon = Icons.Default.Diamond,
        showBadge = true,
        badgeText = "VIP"
    ),
    ProfileMenuItem(
        title = "创作工具",
        subtitle = "AI写作助手和模板",
        icon = Icons.Default.Build
    )
)

fun getSettingsMenuItems() = listOf(
    ProfileMenuItem(
        title = "通知设置",
        subtitle = "管理推送和提醒",
        icon = Icons.Default.Notifications
    ),
    ProfileMenuItem(
        title = "隐私设置",
        subtitle = "账号安全和隐私保护",
        icon = Icons.Default.Security
    ),
    ProfileMenuItem(
        title = "主题设置",
        subtitle = "个性化界面风格",
        icon = Icons.Default.Palette
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
        title = "意见反馈",
        subtitle = "帮助我们改进产品",
        icon = Icons.Default.Feedback
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = menuItem.title,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (menuItem.showBadge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Red
                        ) {
                            Text(
                                text = menuItem.badgeText,
                                fontSize = 8.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                
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