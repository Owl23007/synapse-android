package top.contins.synapse.feature.goal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val category: String = "个人"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen() {
    var goals by remember {
        mutableStateOf(
            listOf(
                Goal("1", "学习Kotlin", "掌握Kotlin语言基础", false, 0.6f, "学习"),
                Goal("2", "健身计划", "每周运动3次", false, 0.3f, "健康"),
                Goal("3", "阅读计划", "每月读2本书", true, 1.0f, "学习")
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 标题栏
        TopAppBar(
            title = {
                Text(
                    text = "目标管理",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { /* 添加新目标 */ }) {
                    Icon(Icons.Default.Add, contentDescription = "添加目标")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        // 统计卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "目标概览",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("总目标")
                        Text(
                            text = goals.size.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text("已完成")
                        Text(
                            text = goals.count { it.isCompleted }.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Column {
                        Text("完成率")
                        Text(
                            text = "${(goals.count { it.isCompleted } * 100 / goals.size)}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 目标列表
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(goals) { goal ->
                GoalItem(
                    goal = goal,
                    onToggleComplete = { goalId ->
                        goals = goals.map {
                            if (it.id == goalId) it.copy(isCompleted = !it.isCompleted)
                            else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalItem(
    goal: Goal,
    onToggleComplete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onToggleComplete(goal.id) }
                ) {
                    Icon(
                        imageVector = if (goal.isCompleted)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (goal.isCompleted) "已完成" else "未完成",
                        tint = if (goal.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = goal.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = goal.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text(goal.category) }
                )
            }

            if (!goal.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("进度")
                        Text("${(goal.progress * 100).toInt()}%")
                    }
                    LinearProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                }
            }
        }
    }
}
