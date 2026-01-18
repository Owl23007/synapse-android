package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.contins.synapse.domain.model.schedule.RepeatRule
import top.contins.synapse.domain.model.schedule.Frequency
import top.contins.synapse.domain.model.schedule.Schedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleDialog(
    initialDate: Long? = null,
    initialSchedule: Schedule? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, Long, Long, String, Boolean, List<Int>?, Boolean, RepeatRule?) -> Unit
) {
    val isEditing = initialSchedule != null

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var selectedStartDateMillis by remember { mutableStateOf<Long?>(initialDate) }
    var selectedEndDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedStartHour by remember { mutableIntStateOf(9) }
    var selectedStartMinute by remember { mutableIntStateOf(0) }
    var selectedEndHour by remember { mutableIntStateOf(10) }
    var selectedEndMinute by remember { mutableIntStateOf(0) }
    
    // 全天事件
    var isAllDay by remember { mutableStateOf(false) }
    
    // 重复设置
    var repeatFrequency by remember { mutableStateOf<Frequency?>(null) }
    var repeatInterval by remember { mutableIntStateOf(1) }
    var repeatEndType by remember { mutableStateOf("never") } // "never", "until", "count"
    var repeatUntilMillis by remember { mutableStateOf<Long?>(null) }
    var repeatCount by remember { mutableIntStateOf(10) }
    var showRepeatSettings by remember { mutableStateOf(false) }
    
    // 提醒设置
    var reminderMinutes by remember { mutableStateOf(listOf<Int>()) }
    var isAlarm by remember { mutableStateOf(false) }
    var showReminderSettings by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialDate) {
        if (initialSchedule == null && initialDate != null) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = initialDate
            
            selectedStartDateMillis = initialDate
            selectedStartHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            selectedStartMinute = cal.get(java.util.Calendar.MINUTE)
            
            // Default 1 hour duration
            cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
            selectedEndDateMillis = cal.timeInMillis
            selectedEndHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
            selectedEndMinute = cal.get(java.util.Calendar.MINUTE)
        }
    }

    LaunchedEffect(initialSchedule) {
        initialSchedule?.let { schedule ->
            title = schedule.title
            location = schedule.location ?: ""
            isAllDay = schedule.isAllDay
            reminderMinutes = schedule.reminderMinutes ?: emptyList()
            isAlarm = schedule.isAlarm

            val startCal = java.util.Calendar.getInstance().apply { timeInMillis = schedule.startTime }
            val endCal = java.util.Calendar.getInstance().apply { timeInMillis = schedule.endTime }

            selectedStartDateMillis = schedule.startTime
            selectedEndDateMillis = schedule.endTime

            if (!schedule.isAllDay) {
                selectedStartHour = startCal.get(java.util.Calendar.HOUR_OF_DAY)
                selectedStartMinute = startCal.get(java.util.Calendar.MINUTE)
                selectedEndHour = endCal.get(java.util.Calendar.HOUR_OF_DAY)
                selectedEndMinute = endCal.get(java.util.Calendar.MINUTE)
            }

            schedule.repeatRule?.let { rule ->
                repeatFrequency = rule.frequency
                repeatInterval = rule.interval
                repeatUntilMillis = rule.until
                repeatCount = rule.count ?: repeatCount
                repeatEndType = when {
                    rule.until != null -> "until"
                    rule.count != null -> "count"
                    else -> "never"
                }
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
                        .padding(top = 16.dp, bottom = 6.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            // 标题区
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Event,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isEditing) "编辑日程" else "新建日程",
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
            
            // 日程标题
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "日程标题",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("今天有什么安排？") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 日期选择（标题和全天开关在同一行）
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "日期",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // 全天开关
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "全天",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                // 统一的日期时间显示
                Surface(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedStartDateMillis != null && selectedEndDateMillis != null) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (selectedStartDateMillis != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (selectedStartDateMillis != null && selectedEndDateMillis != null) {
                                    val dateOnlyFormatter = SimpleDateFormat("MM月dd日", Locale.getDefault())
                                    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    
                                    if (isAllDay) {
                                        // 全天事件：只显示日期
                                        "${dateOnlyFormatter.format(Date(selectedStartDateMillis!!))} - ${dateOnlyFormatter.format(Date(selectedEndDateMillis!!))}"
                                    } else {
                                        // 非全天事件：显示日期+时间
                                        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = selectedStartDateMillis!! }
                                        startCal.set(java.util.Calendar.HOUR_OF_DAY, selectedStartHour)
                                        startCal.set(java.util.Calendar.MINUTE, selectedStartMinute)
                                        
                                        val endCal = java.util.Calendar.getInstance().apply { timeInMillis = selectedEndDateMillis!! }
                                        endCal.set(java.util.Calendar.HOUR_OF_DAY, selectedEndHour)
                                        endCal.set(java.util.Calendar.MINUTE, selectedEndMinute)
                                        
                                        "${dateOnlyFormatter.format(startCal.time)} ${timeFormatter.format(startCal.time)} - ${dateOnlyFormatter.format(endCal.time)} ${timeFormatter.format(endCal.time)}"
                                    }
                                } else {
                                    "点击选择日期时间"
                                },
                                fontWeight = if (selectedStartDateMillis != null) FontWeight.Medium else FontWeight.Normal,
                                color = if (selectedStartDateMillis != null)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                        
                        // 显示详细时间选择按钮（仅非全天事件）
                        if (!isAllDay && selectedStartDateMillis != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 开始时间按钮
                                Surface(
                                    onClick = { showStartTimePicker = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%02d:%02d", selectedStartHour, selectedStartMinute),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                
                                // 结束时间按钮
                                Surface(
                                    onClick = { showEndTimePicker = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%02d:%02d", selectedEndHour, selectedEndMinute),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 地点
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "地点（可选）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("在哪里见面？") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 重复设置
            Surface(
                onClick = { showRepeatSettings = !showRepeatSettings },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "重复",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when {
                                    repeatFrequency == null -> "不重复"
                                    repeatInterval == 1 -> when (repeatFrequency) {
                                        Frequency.DAILY -> "每天"
                                        Frequency.WEEKLY -> "每周"
                                        Frequency.MONTHLY -> "每月"
                                        Frequency.YEARLY -> "每年"
                                        else -> "不重复"
                                    }
                                    else -> when (repeatFrequency) {
                                        Frequency.DAILY -> "每${repeatInterval}天"
                                        Frequency.WEEKLY -> "每${repeatInterval}周"
                                        Frequency.MONTHLY -> "每${repeatInterval}月"
                                        Frequency.YEARLY -> "每${repeatInterval}年"
                                        else -> "不重复"
                                    }
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showRepeatSettings) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            
            if (showRepeatSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp)
                ) {
                    // 重复频率选择
                    Text(
                        "重复频率 (FREQ)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 第一行：不重复、每天、每周
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            null to "不重复",
                            Frequency.DAILY to "每天",
                            Frequency.WEEKLY to "每周"
                        ).forEach { (freq, label) ->
                            FilterChip(
                                selected = repeatFrequency == freq,
                                onClick = { repeatFrequency = freq },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 第二行：每月、每年
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Frequency.MONTHLY to "每月",
                            Frequency.YEARLY to "每年"
                        ).forEach { (freq, label) ->
                            FilterChip(
                                selected = repeatFrequency == freq,
                                onClick = { repeatFrequency = freq },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    if (repeatFrequency != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 重复间隔
                        Text(
                            "间隔 (INTERVAL)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(1, 2, 3, 4).forEach { interval ->
                                FilterChip(
                                    selected = repeatInterval == interval,
                                    onClick = { repeatInterval = interval },
                                    label = { Text(interval.toString(), fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 结束条件
                        Text(
                            "结束条件",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = repeatEndType == "never",
                                onClick = { repeatEndType = "never" },
                                label = { Text("永不", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = repeatEndType == "until",
                                onClick = { repeatEndType = "until" },
                                label = { Text("日期 (UNTIL)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = repeatEndType == "count",
                                onClick = { repeatEndType = "count" },
                                label = { Text("次数 (COUNT)", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        if (repeatEndType == "until") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { /* TODO: 显示日期选择器 */ },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = repeatUntilMillis?.let { dateFormatter.format(Date(it)) } ?: "选择结束日期",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        if (repeatEndType == "count") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(5, 10, 20, 30).forEach { count ->
                                    FilterChip(
                                        selected = repeatCount == count,
                                        onClick = { repeatCount = count },
                                        label = { Text("${count}次", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 提醒设置
            Surface(
                onClick = { showReminderSettings = !showReminderSettings },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "提醒",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (reminderMinutes.isEmpty()) "无提醒" else "${reminderMinutes.size}个提醒",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showReminderSettings) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null
                    )
                }
            }
            
            if (showReminderSettings) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp)
                ) {
                    Text(
                        "提前提醒（可多选）",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val reminderOptions = listOf(
                        0 to "准时",
                        5 to "5分钟前",
                        15 to "15分钟前",
                        30 to "30分钟前",
                        60 to "1小时前",
                        1440 to "1天前"
                    )
                    
                    reminderOptions.chunked(3).forEach { rowOptions ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowOptions.forEach { (minutes, label) ->
                                FilterChip(
                                    selected = reminderMinutes.contains(minutes),
                                    onClick = {
                                        reminderMinutes = if (reminderMinutes.contains(minutes)) {
                                            reminderMinutes - minutes
                                        } else {
                                            (reminderMinutes + minutes).sorted()
                                        }
                                    },
                                    label = { Text(label, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // 填充空白
                            repeat(3 - rowOptions.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // 闹钟提醒开关
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "强力提醒 (闹",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "在事件开始时提醒",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAlarm,
                            onCheckedChange = { isAlarm = it }
                        )
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("取消", style = MaterialTheme.typography.bodyLarge)
                }
                
                Button(
                    onClick = { 
                        if (title.isNotBlank() && selectedStartDateMillis != null && selectedEndDateMillis != null) {
                            val startCalendar = java.util.Calendar.getInstance()
                            startCalendar.timeInMillis = selectedStartDateMillis!!
                            
                            val endCalendar = java.util.Calendar.getInstance()
                            endCalendar.timeInMillis = selectedEndDateMillis!!
                            
                            if (isAllDay) {
                                // 全天事件：开始时间为当天 00:00:00，结束时间为结束日期的 23:59:59
                                startCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                startCalendar.set(java.util.Calendar.MINUTE, 0)
                                startCalendar.set(java.util.Calendar.SECOND, 0)
                                
                                endCalendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                                endCalendar.set(java.util.Calendar.MINUTE, 59)
                                endCalendar.set(java.util.Calendar.SECOND, 59)
                            } else {
                                // 非全天事件：使用选择的时间
                                startCalendar.set(java.util.Calendar.HOUR_OF_DAY, selectedStartHour)
                                startCalendar.set(java.util.Calendar.MINUTE, selectedStartMinute)
                                startCalendar.set(java.util.Calendar.SECOND, 0)
                                
                                endCalendar.set(java.util.Calendar.HOUR_OF_DAY, selectedEndHour)
                                endCalendar.set(java.util.Calendar.MINUTE, selectedEndMinute)
                                endCalendar.set(java.util.Calendar.SECOND, 0)
                            }
                            
                            val startTime = startCalendar.timeInMillis
                            val endTime = endCalendar.timeInMillis
                            
                            // 构建符合 RFC 5545 的重复规则
                            val repeatRule = repeatFrequency?.let {
                                RepeatRule(
                                    frequency = it,
                                    interval = repeatInterval,
                                    until = if (repeatEndType == "until") repeatUntilMillis else null,
                                    count = if (repeatEndType == "count") repeatCount else null
                                )
                            }
                            
                            onConfirm(
                                title, 
                                startTime, 
                                endTime, 
                                location,
                                isAllDay,
                                if (reminderMinutes.isEmpty()) null else reminderMinutes,
                                isAlarm,
                                repeatRule
                            )
                        }
                    },
                    enabled = title.isNotBlank() && selectedStartDateMillis != null && selectedEndDateMillis != null,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(
                        if (isEditing) "保存" else "确定",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // Start DatePicker Dialog
    if (showStartDatePicker) {
        val currentTime = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedStartDateMillis ?: currentTime,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= currentTime - (24 * 60 * 60 * 1000)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedStartDateMillis = datePickerState.selectedDateMillis
                        // 如果结束日期未设置或早于开始日期，自动设置为开始日期
                        if (selectedEndDateMillis == null || selectedEndDateMillis!! < selectedStartDateMillis!!) {
                            selectedEndDateMillis = selectedStartDateMillis
                        }
                        showStartDatePicker = false
                        // 自动打开结束日期选择器
                        showEndDatePicker = true
                    }
                ) {
                    Text("下一步")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End DatePicker Dialog
    if (showEndDatePicker) {
        val currentTime = selectedStartDateMillis ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedEndDateMillis ?: currentTime,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // 结束日期不能早于开始日期
                    return utcTimeMillis >= (selectedStartDateMillis ?: (currentTime - (24 * 60 * 60 * 1000)))
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedEndDateMillis = datePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Start TimePicker Dialog
    if (showStartTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedStartHour,
            initialMinute = selectedStartMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("选择开始时间") },
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
                        selectedStartHour = timePickerState.hour
                        selectedStartMinute = timePickerState.minute
                        
                        // 自动调整结束时间（至少比开始时间晚1小时）
                        val startMinutes = selectedStartHour * 60 + selectedStartMinute
                        val endMinutes = selectedEndHour * 60 + selectedEndMinute
                        if (endMinutes <= startMinutes) {
                            val newEndMinutes = startMinutes + 60
                            selectedEndHour = (newEndMinutes / 60) % 24
                            selectedEndMinute = newEndMinutes % 60
                        }
                        
                        showStartTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // End TimePicker Dialog
    if (showEndTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedEndHour,
            initialMinute = selectedEndMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("选择结束时间") },
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
                        val newEndHour = timePickerState.hour
                        val newEndMinute = timePickerState.minute
                        
                        // 验证结束时间必须晚于开始时间
                        val startMinutes = selectedStartHour * 60 + selectedStartMinute
                        val endMinutes = newEndHour * 60 + newEndMinute
                        
                        if (endMinutes > startMinutes) {
                            selectedEndHour = newEndHour
                            selectedEndMinute = newEndMinute
                        }
                        
                        showEndTimePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}
