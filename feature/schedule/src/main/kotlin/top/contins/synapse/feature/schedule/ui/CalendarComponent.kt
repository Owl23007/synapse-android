package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class CalendarViewType {
    MONTH, WEEK, DAY
}

enum class EventType(val displayName: String, val color: Color) {
    MEETING("会议", Color(0xFF2196F3)),
    PERSONAL("个人", Color(0xFF4CAF50)),
    WORK("工作", Color(0xFFFF9800)),
    STUDY("学习", Color(0xFF9C27B0)),
    ENTERTAINMENT("娱乐", Color(0xFFE91E63))
}

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    currentDate: LocalDate = LocalDate.now(),
    viewType: CalendarViewType = CalendarViewType.MONTH,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit = { _, _ -> },
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
            CalendarViewType.WEEK -> WeekCalendarView(selectedDate, onDateSelected, onTimeSlotClick, selectedDateTime)
            CalendarViewType.DAY -> DayCalendarView(selectedDate, onDateSelected, onTimeSlotClick, selectedDateTime)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarViewTabs(
    selectedViewType: CalendarViewType,
    onViewTypeSelected: (CalendarViewType) -> Unit
) {
    TabRow(selectedTabIndex = selectedViewType.ordinal) {
        CalendarViewType.values().forEach { viewType ->
            Tab(
                selected = selectedViewType == viewType,
                onClick = { onViewTypeSelected(viewType) },
                text = {
                    Text(
                        text = when (viewType) {
                            CalendarViewType.MONTH -> "月"
                            CalendarViewType.WEEK -> "周"
                            CalendarViewType.DAY -> "日"
                        }
                    )
                }
            )
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
private fun WeekCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    selectedDateTime: LocalDateTime? = null
) {
    val daysOfWeek = remember { daysOfWeek() }
    val weekStart = selectedDate.with(DayOfWeek.MONDAY)
    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

    Column {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDates.forEach { date ->
                WeekDayItem(
                    date = date,
                    isSelected = selectedDate == date,
                    isToday = LocalDate.now() == date,
                    modifier = Modifier.weight(1f),
                    onClick = { onDateSelected(date) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        WeekTimeSlotsView(selectedDate = selectedDate, onTimeSlotClick = onTimeSlotClick, selectedDateTime = selectedDateTime)
    }
}

@Composable
private fun DayCalendarView(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotClick: (LocalDate, LocalTime) -> Unit,
    selectedDateTime: LocalDateTime? = null
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.getDefault())),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        DayTimeSlotsView(selectedDate = selectedDate, onTimeSlotClick = onTimeSlotClick, selectedDateTime = selectedDateTime)
    }
}

@Composable
private fun WeekTimeSlotsView(selectedDate: LocalDate, onTimeSlotClick: (LocalDate, LocalTime) -> Unit, selectedDateTime: LocalDateTime? = null) {
    val timeSlots = (8..18).map { hour -> LocalTime.of(hour, 0) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        timeSlots.forEach { time ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    modifier = Modifier.width(60.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp)
                        .clickable { onTimeSlotClick(selectedDate, time) }
                ) {
                    if (time.hour == 9 && selectedDate == LocalDate.now()) {
                        Text(
                            text = "团队周会",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DayTimeSlotsView(selectedDate: LocalDate, onTimeSlotClick: (LocalDate, LocalTime) -> Unit, selectedDateTime: LocalDateTime? = null) {
    val timeSlots = (8..18).map { hour -> LocalTime.of(hour, 0) }

    Column {
        timeSlots.forEach { time ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                    modifier = Modifier.width(60.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (time.hour == 9 && selectedDate == LocalDate.now()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = EventType.MEETING.color.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📅 团队周会",
                                    fontSize = 16.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                                Text(
                                    text = "会议室A · 工作",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (time.hour == 14 && selectedDate == LocalDate.now()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = EventType.STUDY.color.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📚 学习 Kotlin 协程",
                                    fontSize = 16.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                                Text(
                                    text = "线上课程",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // 点击时间槽
                    Box(modifier = Modifier
                        .matchParentSize()
                        .clickable { onTimeSlotClick(selectedDate, time) })
                }
            }
        }
    }
}

@Composable
private fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
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

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = backgroundColor)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor
        )
    }
}

@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean = false,
    modifier: Modifier = Modifier,
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

    Column(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            fontSize = 12.sp,
            color = textColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 16.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = textColor
        )
    }
}
