package top.contins.synapse.feature.task.ui

import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Sort

private enum class SortOption(val label: String) {
    DEFAULT("默认"),
    DATE("日期"),
    PRIORITY("优先级")
}

private enum class FilterOption(val label: String) {
    ALL("全部"),
    ACTIVE("待办"),
    COMPLETED("已完成"),
    ARCHIVED("已归档")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTab(
    tasks: List<Task>,
    onTaskStatusChange: (Task, Boolean) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskEdit: (Task) -> Unit,
    onTaskArchive: (Task) -> Unit = {},
    onTaskUnarchive: (Task) -> Unit = {}
) {
    var sortOption by remember { mutableStateOf(SortOption.DEFAULT) }
    var filterOption by remember { mutableStateOf(FilterOption.ALL) }

    val processedTasks = remember(tasks, sortOption, filterOption) {
        tasks.filter { task ->
            when (filterOption) {
                FilterOption.ALL -> task.status != TaskStatus.ARCHIVED // 默认也不显示归档
                FilterOption.ACTIVE -> task.status != TaskStatus.COMPLETED && 
                                     task.status != TaskStatus.CANCELLED && 
                                     task.status != TaskStatus.ARCHIVED
                FilterOption.COMPLETED -> task.status == TaskStatus.COMPLETED
                FilterOption.ARCHIVED -> task.status == TaskStatus.ARCHIVED
            }
        }.sortedWith(
            when (sortOption) {
                SortOption.DEFAULT -> compareBy { it.status == TaskStatus.COMPLETED }
                SortOption.DATE -> compareBy<Task> { it.status == TaskStatus.COMPLETED }
                    .thenBy { it.dueDate ?: Date(Long.MAX_VALUE) }
                SortOption.PRIORITY -> compareBy<Task> { it.status == TaskStatus.COMPLETED }
                    .thenByDescending { it.priority.ordinal }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TaskFilterBar(
            currentFilter = filterOption,
            onFilterChange = { filterOption = it },
            currentSort = sortOption,
            onSortChange = { sortOption = it }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // 减小卡片间距
        ) {
            items(processedTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onStatusChange = { onTaskStatusChange(task, it) },
                    onDelete = { onTaskDelete(task) },
                    onEdit = { onTaskEdit(task) },
                    onArchive = { onTaskArchive(task) },
                    onUnarchive = { onTaskUnarchive(task) }
                )
            }
        }
    }
}

@Composable
private fun TaskFilterBar(
    currentFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit,
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp), // 减少上下 padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 使用 ScrollableRow 如果筛选过多，这里简单起见假设放得下，或者可以用 LazyRow
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(FilterOption.values()) { option ->
                     FilterChip(
                        selected = currentFilter == option,
                        onClick = { onFilterChange(option) },
                        label = { Text(option.label) },
                        leadingIcon = if (currentFilter == option) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        // 缩小 Chip 的高度和 padding 以显得更紧凑
                        modifier = Modifier.height(32.dp),
                        border = FilterChipDefaults.filterChipBorder(
                             enabled = true,
                             selected = currentFilter == option,
                             borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Sort Button (Compact)
        Box {
            var expanded by remember { mutableStateOf(false) }
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(32.dp) // 缩小按钮尺寸
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                SortOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                option.label,
                                fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        onClick = { 
                            onSortChange(option)
                            expanded = false
                        },
                        trailingIcon = if (currentSort == option) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit
) {
    val currentTask by rememberUpdatedState(task)
    val currentOnStatusChange by rememberUpdatedState(onStatusChange)
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnArchive by rememberUpdatedState(onArchive)
    val currentOnUnarchive by rememberUpdatedState(onUnarchive)
    val scope = rememberCoroutineScope()
    
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isArchived = task.status == TaskStatus.ARCHIVED
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (isArchived) {
                 when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> { // 右滑：取消归档
                         currentOnUnarchive()
                         true
                    }
                    SwipeToDismissBoxValue.EndToStart -> { // 左滑：彻底删除
                        currentOnDelete()
                        true
                    }
                    else -> false
                 }
            } else {
                val isTaskCompleted = currentTask.status == TaskStatus.COMPLETED
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> { // 右滑：完成/取消完成
                        if (isTaskCompleted) {
                            scope.launch {
                                delay(300)
                                currentOnStatusChange(false)
                            }
                            false 
                        } else {
                            // 完成
                            scope.launch {
                                delay(300)
                                currentOnStatusChange(true)
                            }
                            false
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> { // 左滑
                        if (isTaskCompleted) {
                            // 已完成 -> 归档
                             currentOnArchive()
                             true
                        } else {
                            // 未完成 -> 删除
                            currentOnDelete()
                            true
                        }
                    }
                    else -> false
                }
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
            
            // 右滑：
            //   未归档 & 未完成 -> 绿色打钩(完成)
            //   未归档 & 已完成 -> 灰色叉号(取消完成/重做)
            //   已归档 -> 蓝色(全部取出/取消归档)
            
            // 左滑：
            //   未归档 & 未完成 -> 红色垃圾桶(删除)
            //   未归档 & 已完成 -> 紫色(归档)
            //   已归档 -> 红色垃圾桶(删除)
            val (baseColor, icon, iconTint) = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (isArchived) Triple(Color(0xFF2196F3), Icons.Default.Unarchive, Color.White)
                    else if (isCompleted) Triple(Color.LightGray, Icons.Default.Close, Color.Black)
                    else Triple(Color(0xFF4CAF50), Icons.Default.Check, Color.White)
                }
                SwipeToDismissBoxValue.EndToStart -> {
                     if (isArchived) Triple(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, MaterialTheme.colorScheme.error)
                     else if (isCompleted) Triple(Color(0xFF9C27B0), Icons.Default.Archive, Color.White) // 归档色
                     else Triple(MaterialTheme.colorScheme.errorContainer, Icons.Default.Delete, MaterialTheme.colorScheme.error)
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
                    .background(baseColor.copy(alpha = if (direction == SwipeToDismissBoxValue.Settled) 0f else alpha), RoundedCornerShape(12.dp)) // 减小圆角
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
            shape = RoundedCornerShape(12.dp), // 减小圆角
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)), // 添加边框以分离背景
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // 去除阴影，使用边框和平面风格
        ) {
            // 将 alpha 移至内部内容，实现文字淡化效果
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .alpha(if (isCompleted || isArchived) 0.6f else 1f)
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
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // 减小间距
                    ) {
                        // 如果是归档状态，不显示Checkbox，显示归档标识
                        if (isArchived) {
                             Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archived",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                             )
                        } else {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = if (isArchived) null else onStatusChange,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.size(20.dp) // 调整 checkbox 大小
                            )
                        }

                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp, // 调整字体大小
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
                        Spacer(modifier = Modifier.height(4.dp)) // 减小间距
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall, // 使用 bodySmall
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 32.dp) // 调整对齐
                        )
                    }
                    
                    // 显示时间信息：创建时间、截止时间、完成时间
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(start = 32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.spacedBy(12.dp) // 增加信息间距
                    ) {
                         if (isArchived) {
                            // 归档状态：显示创建时间和完成时间
                            TaskTimeInfo(label = "创建", date = task.createdAt)
                            task.completedAt?.let { TaskTimeInfo(label = "完成", date = it) }
                            task.dueDate?.let { TaskTimeInfo(label = "截止", date = it, showIcon = true) }
                         } else {
                             // 普通状态：显示截止时间
                             task.dueDate?.let { dueDate ->
                                val isOverdue = !isCompleted && dueDate.before(Date())
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
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
}

@Composable
fun TaskTimeInfo(label: String, date: Date, showIcon: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (showIcon) {
             Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
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

private fun formatCompactDate(date: Date): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = date }
    val isSameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    
    val pattern = if (isSameYear) "MM-dd" else "yyyy-MM-dd"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
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
