package top.contins.synapse.domain.usecase.schedule

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class GetSchedulesUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    operator fun invoke(): Flow<List<Schedule>> {
        return repository.getAllSchedules()
    }

    fun inTimeRange(start: Long, end: Long): Flow<List<Schedule>> {
        return repository.getSchedulesInTimeRange(start, end)
    }

    fun inTimeRangeForCalendar(calendarId: String, start: Long, end: Long): Flow<List<Schedule>> {
        return repository.getSchedulesInTimeRangeForCalendar(calendarId, start, end)
    }
}
