package top.contins.synapse.feature.task.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.contins.synapse.domain.model.task.Task
import top.contins.synapse.domain.model.task.TaskStatus
import top.contins.synapse.core.ui.compose.task.TaskCard
import top.contins.synapse.core.ui.compose.task.TaskCardCallbacks
import java.util.Date

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
                    callbacks = TaskCardCallbacks(
                        onStatusChange = { onTaskStatusChange(task, it) },
                        onDelete = { onTaskDelete(task) },
                        onEdit = { onTaskEdit(task) },
                        onArchive = { onTaskArchive(task) },
                        onUnarchive = { onTaskUnarchive(task) }
                    )
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
