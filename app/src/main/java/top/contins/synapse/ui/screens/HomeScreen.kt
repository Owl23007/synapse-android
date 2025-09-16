package top.contins.synapse.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import top.contins.synapse.ui.navigation.BottomNavigation
import top.contins.synapse.ui.screens.tabs.*

/**
 * 主屏幕 - 包含底部导航栏的5个Tab界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 根据当前路由获取标题
    val currentTitle = when (currentRoute) {
        "square" -> "🌍 广场"
        "writing" -> "📝 写作"
        "chat" -> "💬 对话"
        "plan" -> "📅 计划"
        "profile" -> "👤 我的"
        else -> "Synapse"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    when (currentRoute) {
                        "square" -> {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Notifications, contentDescription = "通知")
                            }
                        }
                        "writing" -> {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Settings, contentDescription = "设置")
                            }
                        }
                        "chat" -> {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Add, contentDescription = "新建对话")
                            }
                        }
                        "plan" -> {
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "日历视图")
                            }
                            IconButton(onClick = { }) {
                                Icon(Icons.Default.Add, contentDescription = "添加计划")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "square",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("square") {
                SquareScreen()
            }
            composable("writing") {
                WritingScreen()
            }
            composable("chat") {
                ChatScreen()
            }
            composable("plan") {
                PlanScreen()
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = BottomNavigation.items
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (currentRoute == item.route) {
                            item.iconSelected
                        } else {
                            item.iconUnselected
                        },
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // 避免重复的导航栈
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
