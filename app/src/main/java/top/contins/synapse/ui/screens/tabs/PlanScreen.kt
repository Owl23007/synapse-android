package top.contins.synapse.ui.screens.tabs

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.contins.synapse.feature.schedule.ui.ScheduleScreen

/**
 * 计划页面 - 未来扩展：写作日程、任务、目标追踪
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("今日", "日程", "任务", "目标")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab栏
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> TodayTab()
            1 -> ScheduleScreen()
            2 -> TaskTab()
            3 -> GoalTab()
        }
    }
}

@Composable
fun TodayTab() {
    // 使用简化的日期显示，避免Java 8 Time API
    val todayText = "2024年01月16日"
    val dayOfWeekText = "星期二"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 日期显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = todayText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = dayOfWeekText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // 今日概览
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodayStatCard("待办事项", "5", "2已完成", modifier = Modifier.weight(1f))
            TodayStatCard("会议安排", "3", "1即将开始", modifier = Modifier.weight(1f))
        }
        
        // 今日任务列表
        Text(
            text = "今日任务",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getTodayTasks()) { task ->
                TodayTaskCard(task = task)
            }
        }
    }
}

@Composable
fun TaskTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getTaskItems()) { task ->
            TaskCard(task = task)
        }
    }
}

@Composable
fun GoalTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(getGoalItems()) { goal ->
            GoalCard(goal = goal)
        }
    }
}

// 数据类
data class TodayTask(
    val title: String,
    val time: String,
    val isCompleted: Boolean,
    val priority: String
)

data class TaskItem(
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: String,
    val isCompleted: Boolean
)

data class GoalItem(
    val title: String,
    val description: String,
    val progress: Float,
    val deadline: String
)

// 工具函数
fun getDayOfWeekInChinese(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        7 -> "日"
        else -> ""
    }
}

fun getTodayTasks() = listOf(
    TodayTask("完成AI文章写作", "09:00", false, "高"),
    TodayTask("团队会议讨论", "14:00", false, "中"),
    TodayTask("回复客户邮件", "16:00", true, "低"),
    TodayTask("整理学习笔记", "19:00", false, "中")
)

fun getTaskItems() = listOf(
    TaskItem("完成月度总结报告", "撰写本月工作总结和下月计划", "2024-01-30", "高", false),
    TaskItem("更新项目文档", "整理和更新技术文档", "2024-01-28", "中", true),
    TaskItem("学习新技术栈", "深入了解Compose和Kotlin", "2024-02-05", "中", false),
    TaskItem("优化应用性能", "分析并优化应用加载速度", "2024-02-01", "高", false)
)

fun getGoalItems() = listOf(
    GoalItem("提升写作技能", "通过AI助手提高文章质量和效率", 0.7f, "2024-03-31"),
    GoalItem("学习AI应用", "掌握AI工具在工作中的应用", 0.4f, "2024-06-30"),
    GoalItem("完成项目开发", "按时完成Synapse应用开发", 0.6f, "2024-04-30"),
    GoalItem("建立个人品牌", "通过优质内容建立影响力", 0.3f, "2024-12-31")
)

// UI组件
@Composable
fun TodayStatCard(
    title: String,
    count: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTaskCard(task: TodayTask) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 完成状态指示器
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) Color.Green
                        else when (task.priority) {
                            "高" -> Color.Red
                            "中" -> Color.Yellow
                            else -> Color.Gray
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!task.isCompleted) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { }
                )
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "已完成",
                    tint = Color.Green
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(task: TaskItem) {
    Card(
        onClick = { },
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
                    text = task.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (task.priority) {
                            "高" -> Color.Red.copy(alpha = 0.1f)
                            "中" -> Color.Yellow.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = task.priority,
                            fontSize = 10.sp,
                            color = when (task.priority) {
                                "高" -> Color.Red
                                "中" -> Color.Yellow
                                else -> Color.Gray
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (task.isCompleted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "已完成",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = task.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "截止：${task.dueDate}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(goal: GoalItem) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = goal.title,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = goal.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "进度",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(goal.progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LinearProgressIndicator(
                    progress = goal.progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "目标时间：${goal.deadline}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}