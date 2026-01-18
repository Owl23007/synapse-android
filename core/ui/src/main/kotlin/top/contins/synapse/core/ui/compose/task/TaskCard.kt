package top.contins.synapse.core.ui.compose.task

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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

/**
 * 回调集合，统一配置任务卡片的交互能力。
 */
data class TaskCardCallbacks(
    val onStatusChange: ((Boolean) -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
    val onEdit: (() -> Unit)? = null,
    val onArchive: (() -> Unit)? = null,
    val onUnarchive: (() -> Unit)? = null
)

/**
 * 通用任务卡片组件，支持滑动交互、归档/取消归档、编辑等可选功能。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    callbacks: TaskCardCallbacks = TaskCardCallbacks(),
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    val currentTask by rememberUpdatedState(task)
    val currentCallbacks by rememberUpdatedState(callbacks)
    val scope = rememberCoroutineScope()

    val isCompleted = task.status == TaskStatus.COMPLETED
    val isArchived = task.status == TaskStatus.ARCHIVED

    val hasStatusChange = currentCallbacks.onStatusChange != null && !isArchived
    val hasDelete = currentCallbacks.onDelete != null
    val hasArchive = currentCallbacks.onArchive != null
    val hasUnarchive = currentCallbacks.onUnarchive != null && isArchived

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (isArchived) {
                        if (hasUnarchive) {
                            currentCallbacks.onUnarchive?.invoke()
                            true
                        } else {
                            false
                        }
                    } else {
                        if (currentCallbacks.onStatusChange != null) {
                            scope.launch {
                                delay(300)
                                currentCallbacks.onStatusChange?.invoke(!isCompleted)
                            }
                        }
                        false
                    }
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    if (isArchived) {
                        if (hasDelete) {
                            currentCallbacks.onDelete?.invoke()
                            true
                        } else {
                            false
                        }
                    } else if (isCompleted && hasArchive) {
                        currentCallbacks.onArchive?.invoke()
                        true
                    } else if (hasDelete) {
                        currentCallbacks.onDelete?.invoke()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        },
        positionalThreshold = { distance -> distance * 0.55f }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val targetValue = dismissState.targetValue
            val willDismiss = targetValue == SwipeToDismissBoxValue.StartToEnd ||
                targetValue == SwipeToDismissBoxValue.EndToStart

            val (baseColor, icon, iconTint) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> when {
                    isArchived && hasUnarchive -> Triple(Color(0xFF2196F3), Icons.Default.Unarchive, Color.White)
                    !isArchived && currentCallbacks.onStatusChange != null && isCompleted -> Triple(Color.LightGray, Icons.Default.Close, Color.Black)
                    !isArchived && currentCallbacks.onStatusChange != null -> Triple(Color(0xFF4CAF50), Icons.Default.Check, Color.White)
                    else -> Triple(Color.Transparent, Icons.Default.Delete, Color.Transparent)
                }
                SwipeToDismissBoxValue.EndToStart -> when {
                    isArchived && hasDelete -> Triple(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, MaterialTheme.colorScheme.error)
                    !isArchived && isCompleted && hasArchive -> Triple(Color(0xFF9C27B0), Icons.Default.Archive, Color.White)
                    !isArchived && hasDelete -> Triple(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, MaterialTheme.colorScheme.error)
                    else -> Triple(Color.Transparent, Icons.Default.Delete, Color.Transparent)
                }
                else -> Triple(Color.Transparent, Icons.Default.Delete, Color.Transparent)
            }

            val alpha by animateFloatAsState(
                targetValue = if (willDismiss) 1f else 0.5f,
                label = "alpha"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (willDismiss) 1.2f else 1.0f,
                label = "iconScale"
            )

            val rotation = remember { Animatable(0f) }
            LaunchedEffect(willDismiss) {
                if (willDismiss) {
                    repeat(2) {
                        rotation.animateTo(15f, tween(100))
                        rotation.animateTo(-15f, tween(100))
                    }
                    rotation.animateTo(0f, tween(100))
                } else {
                    rotation.animateTo(0f)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseColor.copy(alpha = if (direction == SwipeToDismissBoxValue.Settled) 0f else alpha), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                if (direction != SwipeToDismissBoxValue.Settled && baseColor.alpha > 0f) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .scale(iconScale)
                            .rotate(rotation.value)
                    )
                }
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = currentCallbacks.onEdit
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .alpha(if (isCompleted || isArchived) 0.6f else 1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(getPriorityColor(task.priority))
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            isArchived -> {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(20.dp)
                                )
                            }
                            currentCallbacks.onStatusChange != null -> {
                                Checkbox(
                                    checked = isCompleted,
                                    onCheckedChange = currentCallbacks.onStatusChange,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.width(20.dp)
                                )
                            }
                        }

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                textDecoration = if (isCompleted || isArchived) TextDecoration.LineThrough else null
                            ),
                            color = if (isCompleted || isArchived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        PriorityBadge(task.priority)
                    }

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    if (isArchived) {
                        Row(
                            modifier = Modifier.padding(start = 32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TaskTimeInfo(label = "创建", date = task.createdAt)
                            task.completedAt?.let { TaskTimeInfo(label = "完成", date = it) }
                            task.dueDate?.let { TaskTimeInfo(label = "截止", date = it, showIcon = true) }
                        }
                    } else {
                        task.dueDate?.let { dueDate ->
                            val isOverdue = !isCompleted && dueDate.before(Date())
                            Row(
                                modifier = Modifier.padding(start = 32.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.width(16.dp),
                                    tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatDueDate(dueDate),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isOverdue) {
                                    Text(
                                        text = "已逾期",
                                        style = MaterialTheme.typography.labelSmall,
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
}

@Composable
private fun TaskTimeInfo(label: String, date: Date, showIcon: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (showIcon) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.width(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$label: ${formatCompactDate(date)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val color = getPriorityColor(priority)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = priority.displayName,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

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

private fun formatCompactDate(date: Date): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }
    val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    val pattern = if (isSameYear) "MM-dd" else "yyyy-MM-dd"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}
