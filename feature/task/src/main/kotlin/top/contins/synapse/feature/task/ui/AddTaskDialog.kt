package top.contins.synapse.feature.task.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.contins.synapse.domain.model.task.Task
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialTask: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    val isEditing = initialTask != null
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("中") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableIntStateOf(20) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialTask) {
        initialTask?.let { task ->
            title = task.title
            priority = task.priority.displayName

            task.dueDate?.let { date ->
                val cal = java.util.Calendar.getInstance().apply { time = date }
                selectedDateMillis = date.time
                selectedHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                selectedMinute = cal.get(java.util.Calendar.MINUTE)
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // 标题区
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isEditing) "编辑任务" else "新建任务",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 分隔线
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 20.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // 任务内容区域
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "任务内容",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 任务内容输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("还有什么没做？快想想 ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 优先级选择区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "优先级",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    modifier = Modifier.weight(1f)
                ) {
                    listOf(
                        "紧急" to Color(0xFFEC407A), // Soft Red
                        "高"   to Color(0xFFFFA726), // Soft Orange
                        "中"   to Color(0xFFD4E157), // Soft Amber
                        "低"   to Color(0xFF66BB6A)  // Soft Green
                    ).forEach { (p, color) ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, CircleShape)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.1f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 截止时间选择区域
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 标题行和快捷预设按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "截止时间",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 快捷预设按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 判断是否为今天内
                        val isTodayEnd = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            if (selectedDateMillis == null) return@remember false
                            val calendar = java.util.Calendar.getInstance()
                            val selected = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis!! }
                            calendar.get(java.util.Calendar.YEAR) == selected.get(java.util.Calendar.YEAR) &&
                            calendar.get(java.util.Calendar.DAY_OF_YEAR) == selected.get(java.util.Calendar.DAY_OF_YEAR) &&
                            selectedHour == 23 && selectedMinute == 59
                        }
                        
                        // 判断是否为明天
                        val isTomorrow = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            if (selectedDateMillis == null) return@remember false
                            val calendar = java.util.Calendar.getInstance()
                            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                            val selected = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis!! }
                            calendar.get(java.util.Calendar.YEAR) == selected.get(java.util.Calendar.YEAR) &&
                            calendar.get(java.util.Calendar.DAY_OF_YEAR) == selected.get(java.util.Calendar.DAY_OF_YEAR) &&
                            selectedHour == 20 && selectedMinute == 0
                        }
                        
                        // 判断是否为本周日
                        val isThisWeekEnd = remember(selectedDateMillis, selectedHour, selectedMinute) {
                            if (selectedDateMillis == null) return@remember false
                            val calendar = java.util.Calendar.getInstance()
                            // 找到本周日
                            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                            val daysUntilSunday = if (dayOfWeek == java.util.Calendar.SUNDAY) 0 else 8 - dayOfWeek
                            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysUntilSunday)
                            
                            val selected = java.util.Calendar.getInstance().apply { timeInMillis = selectedDateMillis!! }
                            calendar.get(java.util.Calendar.YEAR) == selected.get(java.util.Calendar.YEAR) &&
                            calendar.get(java.util.Calendar.DAY_OF_YEAR) == selected.get(java.util.Calendar.DAY_OF_YEAR) &&
                            selectedHour == 20 && selectedMinute == 0
                        }
                        
                        // 今天内
                        FilterChip(
                            selected = isTodayEnd,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 23
                                selectedMinute = 59
                            },
                            label = { Text("今天") }
                        )
                        
                        // 明天
                        FilterChip(
                            selected = isTomorrow,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 20
                                selectedMinute = 0
                            },
                            label = { Text("明天") }
                        )
                        
                        // 本周日
                        FilterChip(
                            selected = isThisWeekEnd,
                            onClick = {
                                val calendar = java.util.Calendar.getInstance()
                                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                                val daysUntilSunday = if (dayOfWeek == java.util.Calendar.SUNDAY) 0 else 8 - dayOfWeek
                                calendar.add(java.util.Calendar.DAY_OF_YEAR, daysUntilSunday)
                                selectedDateMillis = calendar.timeInMillis
                                selectedHour = 20
                                selectedMinute = 0
                            },
                            label = { Text("本周") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AnimatedContent(
                    targetState = selectedDateMillis == null,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    },
                    label = "DateSelector"
                ) { isNull ->
                    if (isNull) {
                        Surface(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "设置截止日期",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .clickable { showDatePicker = true }
                                            .padding(vertical = 16.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            dateFormatter.format(selectedDateMillis),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    VerticalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )

                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showTimePicker = true }
                                            .padding(vertical = 16.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            String.format("%02d:%02d", selectedHour, selectedMinute),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            IconButton(onClick = { selectedDateMillis = null }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "清除日期",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 底部按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val dueDate = if (selectedDateMillis != null) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.timeInMillis = selectedDateMillis!!
                                calendar.set(java.util.Calendar.HOUR_OF_DAY, selectedHour)
                                calendar.set(java.util.Calendar.MINUTE, selectedMinute)
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.time)
                            } else null
                            onConfirm(title, priority, dueDate)
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isEditing) "保存" else "创建任务")
                }
            }
        }
    }
    
    // DatePicker Dialog
    if (showDatePicker) {
        val currentTime = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: currentTime,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 允许选今天
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}
