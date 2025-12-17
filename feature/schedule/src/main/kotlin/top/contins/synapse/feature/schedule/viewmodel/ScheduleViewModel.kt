package top.contins.synapse.feature.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.CalendarAccount
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.usecase.calendar.GetCalendarsUseCase
import top.contins.synapse.domain.usecase.schedule.CreateScheduleUseCase
import top.contins.synapse.domain.usecase.schedule.DeleteScheduleUseCase
import top.contins.synapse.domain.usecase.schedule.GetSchedulesUseCase
import top.contins.synapse.domain.usecase.schedule.UpdateScheduleUseCase
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val getSchedulesUseCase: GetSchedulesUseCase,
    private val createScheduleUseCase: CreateScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val getCalendarsUseCase: GetCalendarsUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val calendars: StateFlow<List<CalendarAccount>> = getCalendarsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Fetch all schedules for now, optimization needed for large datasets (fetch by range)
    val schedules: StateFlow<List<Schedule>> = getSchedulesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered schedules for the selected date
    val selectedDateSchedules: StateFlow<List<Schedule>> = combine(
        schedules,
        _selectedDate
    ) { allSchedules, date ->
        val zoneId = java.time.ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        allSchedules.filter { schedule ->
            // Check for overlap
            schedule.startTime < endOfDay && schedule.endTime > startOfDay
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onMonthChanged(month: YearMonth) {
        _currentMonth.value = month
    }

    fun createSchedule(schedule: Schedule) {
        viewModelScope.launch {
            createScheduleUseCase(schedule)
        }
    }

    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            updateScheduleUseCase(schedule)
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            deleteScheduleUseCase(schedule)
        }
    }
}
