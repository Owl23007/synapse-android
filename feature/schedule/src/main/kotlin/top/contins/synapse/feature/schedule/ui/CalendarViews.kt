package top.contins.synapse.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.nlf.calendar.Lunar
import top.contins.synapse.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun MonthView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
) {
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first(),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(day, isSelected = selectedDate == day.date) { clicked ->
                    onDateSelected(clicked.date)
                }
            },
            monthHeader = {
                // Month header if needed
            }
        )
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
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    val isToday = day.date == LocalDate.now()
    
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
                onClick = { onClick(day) }
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
                fontSize = 16.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (lunarText.isNotEmpty()) {
                Text(
                    text = lunarText,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                )
            }
        }
    }
}
