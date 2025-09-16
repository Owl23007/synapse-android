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
            route = "square",
            title = "广场",
            iconSelected = Icons.Filled.Public,
            iconUnselected = Icons.Outlined.Public
        ),
        BottomNavItem(
            route = "writing",
            title = "写作",
            iconSelected = Icons.Filled.Edit,
            iconUnselected = Icons.Outlined.Edit
        ),
        BottomNavItem(
            route = "chat",
            title = "对话",
            iconSelected = Icons.Filled.Chat,
            iconUnselected = Icons.Outlined.Chat
        ),
        BottomNavItem(
            route = "plan",
            title = "计划",
            iconSelected = Icons.Filled.CalendarToday,
            iconUnselected = Icons.Outlined.CalendarToday
        ),
        BottomNavItem(
            route = "profile",
            title = "我的",
            iconSelected = Icons.Filled.Person,
            iconUnselected = Icons.Outlined.Person
        )
    )
}