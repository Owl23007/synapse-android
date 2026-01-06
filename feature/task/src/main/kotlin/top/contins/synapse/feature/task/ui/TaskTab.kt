package top.contins.synapse.feature.task.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { 
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { distance -> distance * 0.7f }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isCompleted) 0.6f else 1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
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
