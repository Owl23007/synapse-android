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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日 EEEE", Locale.CHINESE)),
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
