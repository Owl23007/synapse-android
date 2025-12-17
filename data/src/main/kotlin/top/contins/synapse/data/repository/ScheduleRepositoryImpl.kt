package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.dao.ScheduleDao
import top.contins.synapse.data.local.converter.DataMapper.toDomain
import top.contins.synapse.data.local.converter.DataMapper.toEntity
import top.contins.synapse.domain.model.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getScheduleById(id: String): Schedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    override fun getSchedulesInTimeRange(start: Long, end: Long): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesInTimeRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSchedulesInTimeRangeForCalendar(
        calendarId: String,
        start: Long,
        end: Long
    ): Flow<List<Schedule>> {
        return scheduleDao.getSchedulesInTimeRangeForCalendar(calendarId, start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConflictingSchedules(start: Long, end: Long): List<Schedule> {
        return scheduleDao.getConflictingSchedules(start, end).map { it.toDomain() }
    }

    override fun searchSchedules(query: String): Flow<List<Schedule>> {
        return scheduleDao.searchSchedules(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule.toEntity())
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }

    override suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toEntity())
    }

    override suspend fun deleteScheduleById(id: String) {
        scheduleDao.deleteScheduleById(id)
    }

    override suspend fun deleteSchedulesByCalendarId(calendarId: String) {
        scheduleDao.deleteSchedulesByCalendarId(calendarId)
    }
}
