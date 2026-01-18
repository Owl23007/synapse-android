package top.contins.synapse.ui.home

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import top.contins.synapse.ui.home.components.BottomNavigationBar
import top.contins.synapse.ui.home.plan.PlanScreen
import top.contins.synapse.feature.profile.ProfileScreen
import top.contins.synapse.feature.assistant.ChatScreen
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

/**
 * 主屏幕
 * 
 * 包含底部导航栏和浮动操作按钮的主应用界面
 * - 管理底部导航的路由切换
 * - 提供快速进入聊天功能的浮动按钮
 * - 支持登出操作
 * 
 * 分为三个内部屏幕：计划、聊天和个人资料
 * 计划：PlanScreen 在当前模块中实现
 * 聊天：ChatScreen 在 feature/assistant 模块中实现
 * 个人资料：ProfileScreen 在 feature/profile 模块中实现
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun HomeScreen(
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isChatSelected = currentRoute == "chat"
    
    val isGuest by viewModel.isGuest.collectAsState()
    var showGuestDialog by remember { mutableStateOf(false) }

    val handleNavigation: (String) -> Unit = { route ->
        if (isGuest && (route == "chat" || route == "profile")) {
            showGuestDialog = true
        } else {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("需要登录") },
            text = { Text("该功能需要登录后才能使用。是否立即登录？") },
            confirmButton = {
                TextButton(onClick = {
                    showGuestDialog = false
                    onLogout()
                }) {
                    Text("去登录")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onNavigate = handleNavigation
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    handleNavigation("chat")
                },
                containerColor = if (isChatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isChatSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                ),
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 84.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "对话",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "plan",
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable("plan") {
                PlanScreen()
            }
            composable("chat") {
                ChatScreen()
            }
            composable("profile") {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
