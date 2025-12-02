package top.contins.synapse.ui.navigation

import androidx.compose.material.icons.Icons
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
            iconSelected = Icons.Filled.Chat,
            iconUnselected = Icons.Outlined.Chat
        ),
        BottomNavItem(
            route = "profile",
            title = "我的",
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person
        )
    )
}