package top.contins.synapse.ui.screens.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航栏项目数据类
 */
data class BottomNavItem(
    val route: String,
    val title: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
)

/**
 * 底部导航栏配置
 */
object BottomNavigation {
    val items = listOf(
        BottomNavItem(
            route = "plan",
            title = "计划",
            iconSelected = Icons.Filled.CalendarToday,
            iconUnselected = Icons.Outlined.CalendarToday
        ),
        BottomNavItem(
            route = "chat",
            title = "对话",
            iconSelected = Icons.AutoMirrored.Filled.Chat,
            iconUnselected = Icons.AutoMirrored.Outlined.Chat
        ),
        BottomNavItem(
            route = "profile",
            title = "我的",
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person
        )
    )
}