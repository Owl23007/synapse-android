package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import com.nlf.calendar.Lunar
import java.time.*
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.feature.schedule.util.ScheduleLayoutHelper
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.absoluteOffset

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now(),
    viewType: CalendarViewType = CalendarViewType.MONTH,
    schedules: List<Schedule> = emptyList(),
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit = { _, _ -> },
    onScheduleClick: (Schedule) -> Unit = {},
    onViewTypeChanged: (CalendarViewType) -> Unit = {},
    selectedDateTime: LocalDateTime? = null
) {
    var selectedDate by remember { mutableStateOf(currentDate) }

    Column(modifier = modifier) {
        CalendarViewTabs(
            selectedViewType = viewType,
            onViewTypeSelected = onViewTypeChanged
        )

        when (viewType) {
            CalendarViewType.MONTH -> MonthCalendarView(selectedDate) { onDateSelected(it) }
            CalendarViewType.WEEK -> WeekCalendarView(
                selectedDate = selectedDate, 
                schedules = schedules, 
                onDateSelected = onDateSelected, 
                onTimeSlotClick = onTimeSlotClick, 
                onScheduleClick = onScheduleClick
            )
            CalendarViewType.DAY -> DayCalendarView(
                selectedDate = selectedDate, 
                schedules = schedules, 
                onDateSelected = onDateSelected, 
                onTimeSlotClick = onTimeSlotClick, 
                onScheduleClick = onScheduleClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarViewTabs(
    selectedViewType: CalendarViewType,
    onViewTypeSelected: (CalendarViewType) -> Unit
) {
    val options = CalendarViewType.entries
    val selectedIndex = selectedViewType.ordinal

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        val maxWidth = maxWidth
        val tabWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            label = "indicator"
        )

        // Indicator
        Box(
            modifier = Modifier
                .width(tabWidth)
                .fillMaxHeight()
                .offset(x = indicatorOffset)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        )

        // Text Labels
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEach { type ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onViewTypeSelected(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (type) {
                            CalendarViewType.MONTH -> "月"
                            CalendarViewType.WEEK -> "周"
                            CalendarViewType.DAY -> "日"
                        },
                        color = if (selectedViewType == type) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selectedViewType == type) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    Column {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                CalendarDayItem(
                    day = day,
                    isSelected = selectedDate == day.date,
                    isToday = LocalDate.now() == day.date,
                    onClick = { onDateSelected(day.date) }
                )
            }
        )
    }
}

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val daysOfWeek = remember { daysOfWeek() }
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Week Date Header
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDates.forEach { date ->
                WeekDayItem(
                    modifier = Modifier.weight(1f),
                    date = date,
                    isSelected = selectedDate == date,
                    isToday = LocalDate.now() == date
                ) { onDateSelected(date) }
            }
        }
        
        // Week Scheduler Grid
        WeekTimeSlotsView(
            weekDates = weekDates,
            schedules = schedules, 
            onTimeSlotClick = onTimeSlotClick,
            onScheduleClick = onScheduleClick
        )
    }
}

@Composable
fun DayCalendarView(
    selectedDate: LocalDate,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val (allDaySchedules, timeSchedules) = remember(schedules) { schedules.partition { it.isAllDay } }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(16.dp)) {
             Text(
                text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.CHINESE)),
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        if (allDaySchedules.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 50.dp, end = 16.dp, bottom = 8.dp)) {
                 allDaySchedules.forEach { s ->
                      ScheduleCard(
                          schedule = s, 
                          modifier = Modifier
                              .fillMaxWidth()
                              .padding(bottom = 2.dp)
                              .clickable { onScheduleClick(s) }
                      )
                 }
            }
        }

        DayTimeSlotsView(
            selectedDate = selectedDate, 
            schedules = timeSchedules,
            onTimeSlotClick = onTimeSlotClick,
            onScheduleClick = onScheduleClick,
            showTimeLabels = true
        )
    }
}

@Composable
private fun WeekTimeSlotsView(
    weekDates: List<LocalDate>,
    schedules: List<Schedule>,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val scrollState = rememberScrollState()
    val rowHeight = 60.dp
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        val totalWidth = maxWidth
        val timeLabelWidth = 50.dp
        val colWidth = (totalWidth - timeLabelWidth) / 7
        
        // Time Labels
        Column(modifier = Modifier.width(timeLabelWidth)) {
             (0..23).forEach { hour ->
                Box(modifier = Modifier.height(rowHeight), contentAlignment = Alignment.TopCenter) {
                    Text(
                        text = "%02d:00".format(hour),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top=4.dp)
                    )
                }
             }
        }
        
        // Columns
        Row(modifier = Modifier.padding(start = timeLabelWidth)) {
             weekDates.forEach { date ->
                Box(modifier = Modifier.width(colWidth).height(rowHeight * 24).border(width=0.5.dp, color=Color.LightGray)) {
                    val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val daySchedules = schedules.filter { 
                        it.startTime < endOfDay && it.endTime > startOfDay
                    }
                    val (allDay, timeSpecific) = daySchedules.partition { it.isAllDay }
                    
                    DayScheduleColumn(
                        date = date,
                        schedules = timeSpecific,
                        rowHeight = rowHeight,
                        colWidth = colWidth,
                        onTimeSlotClick = onTimeSlotClick,
                        onScheduleClick = onScheduleClick
                    )
                    
                    if (allDay.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth().zIndex(2f)) {
                           allDay.forEach { s ->
                               ScheduleCard(
                                   schedule = s, 
                                   modifier = Modifier.fillMaxWidth().height(24.dp).padding(1.dp).clickable { onScheduleClick(s) }
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
private fun DayScheduleColumn(
    date: LocalDate,
    schedules: List<Schedule>,
    rowHeight: androidx.compose.ui.unit.Dp,
    colWidth: androidx.compose.ui.unit.Dp,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val positioned = remember(schedules) { ScheduleLayoutHelper.arrangeDaySchedules(schedules) }
    
    Box(modifier = Modifier.fillMaxSize()) {
         Column(modifier = Modifier.fillMaxSize()) {
             (0..23).forEach { h ->
                 Box(modifier = Modifier.weight(1f).fillMaxWidth().clickable { onTimeSlotClick(date, LocalTime.of(h, 0)) })
             }
         }
         
         positioned.forEach { pos ->
             val schedule = pos.schedule
             val zone = ZoneId.systemDefault()
             val start = Instant.ofEpochMilli(schedule.startTime).atZone(zone)
             val end = Instant.ofEpochMilli(schedule.endTime).atZone(zone)
             
             val dayStart = date.atStartOfDay(zone)
             val startMinutes = java.time.Duration.between(dayStart, start).toMinutes().coerceAtLeast(0)
             val endMinutes = java.time.Duration.between(dayStart, end).toMinutes().coerceAtMost(24*60)
             val duration = (endMinutes - startMinutes).coerceAtLeast(15) 
             
             val top = (startMinutes / 60f).toFloat() * rowHeight.value
             val height = (duration / 60f).toFloat() * rowHeight.value
             
             val width = colWidth / pos.totalCols
             val left = width * pos.colIndex
             
             ScheduleCard(
                schedule = schedule,
                modifier = Modifier
                    .absoluteOffset(x = left, y = top.dp)
                    .width(width)
                    .height(height.dp)
                    .zIndex(1f)
                    .clickable { onScheduleClick(schedule) }
             )
         }
    }
}

@Composable
private fun DayTimeSlotsView(
    selectedDate: LocalDate,
    schedules: List<Schedule>,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    onScheduleClick: (Schedule) -> Unit,
    showTimeLabels: Boolean = true
) {
     val scrollState = rememberScrollState()
     val rowHeight = 60.dp
     
     BoxWithConstraints(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
         val labelsWidth = if (showTimeLabels) 50.dp else 0.dp
         val contentWidth = maxWidth - labelsWidth
         
         Row {
             if (showTimeLabels) {
                 Column(modifier = Modifier.width(labelsWidth)) {
                     (0..23).forEach { h ->
                         Box(modifier = Modifier.height(rowHeight), contentAlignment = Alignment.TopCenter) {
                             Text("%02d:00".format(h), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top=4.dp))
                         }
                     }
                 }
             }
             
             Box(modifier = Modifier.width(contentWidth)) {
                Column {
                    (0..23).forEach {
                        Box(
                            modifier = Modifier
                                .height(rowHeight)
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(Color.LightGray, Offset(0f, 0f), Offset(size.width, 0f))
                                }
                        )
                    }
                }
                
                DayScheduleColumn(
                    date = selectedDate,
                    schedules = schedules,
                    rowHeight = rowHeight,
                    colWidth = contentWidth, // Full width
                    onTimeSlotClick = onTimeSlotClick,
                    onScheduleClick = onScheduleClick
                )
             }
         }
     }
}

@Composable
private fun ScheduleCard(
    schedule: Schedule,
    modifier: Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.padding(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.8f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Text(
                text = schedule.title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface
        else -> Color.Gray
    }

    val lunarText = remember(day.date) {
        if (day.position == DayPosition.MonthDate) {
            try {
                val date = Date.from(day.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val lunar = Lunar.fromDate(date)
                lunar.dayInChinese
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (lunarText.isNotEmpty()) {
                Text(
                    text = lunarText,
                    color = textColor.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
private fun WeekDayItem(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val lunarText = remember(date) {
        try {
            val d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val lunar = Lunar.fromDate(d)
            lunar.dayInChinese
        } catch (e: Exception) {
            ""
        }
    }

    Column(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
            fontSize = 12.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        if (lunarText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = lunarText,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}
