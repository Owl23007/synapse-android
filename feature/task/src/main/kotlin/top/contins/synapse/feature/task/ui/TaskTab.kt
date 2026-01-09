package top.contins.synapse.feature.task.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.task.Task
import top.contins.synapse.domain.model.task.TaskPriority
import top.contins.synapse.domain.model.task.TaskStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TaskTab(
    tasks: List<Task>,
    onTaskStatusChange: (Task, Boolean) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskEdit: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                onStatusChange = { onTaskStatusChange(task, it) },
                onDelete = { onTaskDelete(task) },
                onEdit = { onTaskEdit(task) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val currentTask by rememberUpdatedState(task)
    val currentOnStatusChange by rememberUpdatedState(onStatusChange)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val scope = rememberCoroutineScope()
    
    val isCompleted = task.status == TaskStatus.COMPLETED
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            val isTaskCompleted = currentTask.status == TaskStatus.COMPLETED
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> { // 右滑：完成/取消完成
                    scope.launch {
                        delay(300) // 等待回弹动画
                        currentOnStatusChange(!isTaskCompleted)
                    }
                    false // 无论如何都回弹，不删除条目
                }
                SwipeToDismissBoxValue.EndToStart -> { // 左滑：删除
                    currentOnDelete()
                    true // 确认删除，条目移出
                }
                else -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.55f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val targetValue = dismissState.targetValue
            // 检测是否达到阈值：targetValue 变为具体的方向值（非 Settled）
            val willDismiss = targetValue == SwipeToDismissBoxValue.StartToEnd || 
                              targetValue == SwipeToDismissBoxValue.EndToStart
            
            // 右滑：绿色打钩(完成) 或 灰色叉号(取消完成)
            // 左滑：红色垃圾桶(删除)
            val (baseColor, icon, iconTint) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (isCompleted) Triple(Color.LightGray, Icons.Default.Close, Color.Black)
                    else Triple(Color(0xFF4CAF50), Icons.Default.Check, Color.White)
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    Triple(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, MaterialTheme.colorScheme.error)
                }
                else -> Triple(Color.Transparent, Icons.Default.Delete, Color.Transparent)
            }

            // 未达到阈值时透明度降低，达到阈值时完全不透明
            val alpha by animateFloatAsState(
                targetValue = if (willDismiss) 1f else 0.5f,
                label = "alpha"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (willDismiss) 1.2f else 1.0f,
                label = "iconScale"
            )

            val rotate = remember { Animatable(0f) }
            LaunchedEffect(willDismiss) {
                if (willDismiss) {
                    repeat(2) {
                        rotate.animateTo(15f, tween(100))
                        rotate.animateTo(-15f, tween(100))
                    }
                    rotate.animateTo(0f, tween(100))
                } else {
                    rotate.animateTo(0f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseColor.copy(alpha = if (direction == SwipeToDismissBoxValue.Settled) 0f else alpha), RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                if (direction != SwipeToDismissBoxValue.Settled) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .scale(iconScale)
                            .rotate(rotate.value)
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // 移除 Card 的 alpha，防止滑动背景色透出
                .combinedClickable(
                    onClick = {},
                    onLongClick = onEdit
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // 将 alpha 移至内部内容，实现文字淡化效果
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .alpha(if (isCompleted) 0.6f else 1f)
            ) {
                // 优先级垂直色条
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(getPriorityColor(task.priority))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = onStatusChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            ),
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        PriorityBadge(task.priority)
                    }

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 44.dp)
                        )
                    }

                    task.dueDate?.let { dueDate ->
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.padding(start = 44.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isOverdue = !isCompleted && dueDate.before(Date())
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDueDate(dueDate),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (isOverdue) {
                                Text(
                                    text = "· 已逾期",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val color = getPriorityColor(priority)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = priority.displayName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun getPriorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.URGENT -> Color(0xFFD32F2F)
        TaskPriority.HIGH -> Color(0xFFF57C00)
        TaskPriority.MEDIUM -> Color(0xFFFBC02D)
        TaskPriority.LOW -> Color(0xFF388E3C)
    }
}

private fun formatDueDate(date: Date): String {
    val now = Calendar.getInstance()
    val due = Calendar.getInstance().apply { time = date }
    
    val isSameDay = now.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)
                    
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    val isTomorrow = tomorrow.get(Calendar.YEAR) == due.get(Calendar.YEAR) &&
                     tomorrow.get(Calendar.DAY_OF_YEAR) == due.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "今天"
        isTomorrow -> "明天"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }
}
