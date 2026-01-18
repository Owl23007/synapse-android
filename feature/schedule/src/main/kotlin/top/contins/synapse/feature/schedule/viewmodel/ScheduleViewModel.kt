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
import top.contins.synapse.domain.model.schedule.CalendarAccount
import top.contins.synapse.domain.model.schedule.Schedule
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
    private val checkScheduleConflictUseCase: top.contins.synapse.domain.usecase.schedule.CheckScheduleConflictUseCase,
    private val getCalendarsUseCase: GetCalendarsUseCase,
    private val batteryOptimizationHelper: top.contins.synapse.feature.schedule.util.BatteryOptimizationHelper
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

    // 目前获取所有日程，对于大数据集需要优化（按范围获取）
    val schedules: StateFlow<List<Schedule>> = getSchedulesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 选中日期的日程过滤
    val selectedDateSchedules: StateFlow<List<Schedule>> = combine(
        schedules,
        _selectedDate
    ) { allSchedules, date ->
        val zoneId = java.time.ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        allSchedules.filter { schedule ->
            // 检查重叠
            schedule.startTime < endOfDay && schedule.endTime > startOfDay
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 选中周的日程过滤
    val weekSchedules: StateFlow<List<Schedule>> = combine(
        schedules,
        _selectedDate
    ) { allSchedules, date ->
        val zoneId = java.time.ZoneId.systemDefault()
        val startOfWeek = date.with(java.time.DayOfWeek.MONDAY).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfWeek = date.with(java.time.DayOfWeek.SUNDAY).plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        allSchedules.filter { schedule ->
            schedule.startTime < endOfWeek && schedule.endTime > startOfWeek
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun checkConflict(start: Long, end: Long): List<Schedule> {
        return checkScheduleConflictUseCase(start, end)
    }

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

    // 检查电池优化状态
    fun isBatteryOptimizationIgnored(): Boolean {
        return batteryOptimizationHelper.isIgnoringBatteryOptimizations()
    }

    // 请求忽略电池优化
    fun requestIgnoreBatteryOptimizations(onStartActivity: (android.content.Intent) -> Unit) {
        batteryOptimizationHelper.requestIgnoreBatteryOptimizations(onStartActivity)
    }
}
