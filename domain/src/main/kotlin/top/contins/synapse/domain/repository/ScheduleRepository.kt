package top.contins.synapse.domain.repository

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.Schedule

interface ScheduleRepository {
    fun getAllSchedules(): Flow<List<Schedule>>
    suspend fun getScheduleById(id: String): Schedule?
    fun getSchedulesInTimeRange(start: Long, end: Long): Flow<List<Schedule>>
    fun getSchedulesInTimeRangeForCalendar(calendarId: String, start: Long, end: Long): Flow<List<Schedule>>
    suspend fun getConflictingSchedules(start: Long, end: Long): List<Schedule>
    fun searchSchedules(query: String): Flow<List<Schedule>>
    suspend fun insertSchedule(schedule: Schedule)
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(schedule: Schedule)
    suspend fun deleteScheduleById(id: String)
    suspend fun deleteSchedulesByCalendarId(calendarId: String)
}
