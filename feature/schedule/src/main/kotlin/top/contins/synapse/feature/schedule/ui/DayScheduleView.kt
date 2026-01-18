package top.contins.synapse.feature.schedule.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.contins.synapse.domain.model.schedule.Schedule
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DayScheduleView(
    animatedVisibilityScope: AnimatedVisibilityScope,
    date: LocalDate,
    schedules: List<Schedule>,
    onBack: () -> Unit,
    onAddSchedule: (Long) -> Unit,
    onEditSchedule: (Schedule) -> Unit
) {
    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Shared Element Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            // This Text matches the Day cell number
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "day-${date}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Additional Date Info (Not shared)
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月 EEEE")),
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Timeline
        WeekTimeSlotsView(
            weekDates = listOf(date),
            schedules = schedules,
            onTimeSlotClick = { d, t -> onAddSchedule(d.atTime(t).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()) },
            onScheduleClick = onEditSchedule
        )
    }
}
