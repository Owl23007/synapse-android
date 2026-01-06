package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.yearMonth
import com.nlf.calendar.Lunar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.contins.synapse.domain.model.schedule.Schedule
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.snapshotFlow
import java.time.temporal.ChronoUnit

private val MIN_MONTH = YearMonth.of(1900, 1)
private val MAX_MONTH = YearMonth.of(2200, 12)
private val MONTH_COUNT = ChronoUnit.MONTHS.between(MIN_MONTH, MAX_MONTH).toInt()
private val lunarCache = ConcurrentHashMap<LocalDate, String>()

@Composable
fun MonthView(
    modifier: Modifier = Modifier,
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
) {
    val daysOfWeek = remember { daysOfWeek() }
    val schedulesMap = remember(schedules) {
        val map = mutableMapOf<LocalDate, MutableList<Schedule>>()
        val zoneId = ZoneId.systemDefault()
        schedules.forEach { schedule ->
            val startDate = Instant.ofEpochMilli(schedule.startTime).atZone(zoneId).toLocalDate()
            val endInstant = if (schedule.endTime > schedule.startTime) schedule.endTime - 1 else schedule.startTime
            val endDate = Instant.ofEpochMilli(endInstant).atZone(zoneId).toLocalDate()

            var date = startDate
            while (!date.isAfter(endDate)) {
                map.getOrPut(date) { mutableListOf() }.add(schedule)
                date = date.plusDays(1)
            }
        }
        map
    }

    val initialPage = remember(Unit) {
        ChronoUnit.MONTHS.between(MIN_MONTH, currentMonth).toInt().coerceIn(0, MONTH_COUNT - 1)
    }
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { MONTH_COUNT }
    )

    // Sync external currentMonth change to Pager
    LaunchedEffect(currentMonth) {
        val targetPage = ChronoUnit.MONTHS.between(MIN_MONTH, currentMonth).toInt().coerceIn(0, MONTH_COUNT - 1)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync Pager change to external onMonthChanged
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val newMonth = MIN_MONTH.plusMonths(page.toLong())
            if (newMonth != currentMonth) {
                onMonthChanged(newMonth)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { onMonthChanged(currentMonth.minusMonths(1)) },
            onNextMonth = { onMonthChanged(currentMonth.plusMonths(1)) },
            onPreviousYear = { onMonthChanged(currentMonth.minusYears(1)) },
            onNextYear = { onMonthChanged(currentMonth.plusYears(1)) },
            onTodayClick = {
                onDateSelected(LocalDate.now())
                onMonthChanged(YearMonth.now())
            }
        )
        
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = 3, // Preload 3 pages left and right
            verticalAlignment = Alignment.Top
        ) { page ->
            val monthForPage = MIN_MONTH.plusMonths(page.toLong())
            val weeks = remember(monthForPage) {
                val firstDayOfWeek = daysOfWeek.first()
                val firstDayOfMonth = monthForPage.atDay(1)
                var current = firstDayOfMonth
                // Adjust to the start of the week
                val diff = (current.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
                current = current.minusDays(diff.toLong())

                val allWeeks = mutableListOf<List<LocalDate>>()
                
                while (true) {
                    val week = (0 until 7).map {
                        val d = current
                        current = current.plusDays(1)
                        d
                    }
                    allWeeks.add(week)
                    
                    // Generate at least 5 weeks.
                    val lastDay = week.last()
                    if (lastDay.yearMonth.isAfter(monthForPage) || (lastDay.monthValue == monthForPage.monthValue && lastDay.dayOfMonth == monthForPage.lengthOfMonth())) {
                        if (allWeeks.size >= 5) break
                    }
                }
                allWeeks
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                weeks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            val isCurrentMonth = date.month == monthForPage.month
                            Box(modifier = Modifier.weight(1f)) {
                                Day(
                                    date = date,
                                    isCurrentMonth = isCurrentMonth,
                                    isSelected = selectedDate == date,
                                    schedules = schedulesMap[date] ?: emptyList(),
                                    onClick = { clickedDate ->
                                        onDateSelected(clickedDate)
                                        if (!isCurrentMonth) {
                                            // When clicking a gray date, we might want to switch to that month?
                                            // Leaving as per previous logic (only select), but usually good UX is to switch.
                                            // The user didn't ask to change this logic, just "add left/right scroll".
                                            // Wait, if I click previous month date, pager should technically scroll?
                                            // Let's stick to update selectedDate. Parent might update currentMonth if they want.
                                            onMonthChanged(YearMonth.from(clickedDate))
                                        }
                                    }
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
fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousYear) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Year")
            }
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
        }

        Text(
            text = "${currentMonth.year}年${currentMonth.monthValue}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
            IconButton(onClick = onNextYear) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
            }
        }
        
        OutlinedButton(
            onClick = onTodayClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("今天")
        }
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun Day(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    schedules: List<Schedule>,
    onClick: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    
    val backgroundColor = if (isToday) Color(0xFFE3F2FD) else Color.Transparent
    
    val borderModifier = if (isSelected) {
        Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    val textColor = when {
        !isCurrentMonth -> Color.LightGray
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal

    var lunarText by remember(date) { mutableStateOf(lunarCache[date] ?: "") }

    LaunchedEffect(date) {
        if (isCurrentMonth && lunarText.isEmpty()) {
            val cached = lunarCache[date]
            if (cached != null) {
                lunarText = cached
            } else {
                val text = withContext(Dispatchers.Default) {
                    try {
                        val d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        val lunar = Lunar.fromDate(d)
                        lunar.dayInChinese
                    } catch (_: Exception) {
                        ""
                    }
                }
                lunarCache[date] = text
                lunarText = text
            }
        }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color = backgroundColor)
            .then(borderModifier)
            .clickable(
                enabled = isCurrentMonth, 
                onClick = { onClick(date) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 16.sp,
                fontWeight = fontWeight
            )
            if (lunarText.isNotEmpty() && isCurrentMonth) {
                Text(
                    text = lunarText,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }
            
            if (schedules.isNotEmpty() && isCurrentMonth) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    schedules.take(3).forEach { schedule ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(schedule.color ?: 0xFF2196F3))
                        )
                    }
                }
            }
        }
    }
}

private fun daysOfWeek(): List<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    return (0..6L).map { firstDayOfWeek.plus(it) }
}
