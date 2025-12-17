package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.contins.synapse.data.local.dao.CalendarDao
import top.contins.synapse.data.local.converter.DataMapper.toDomain
import top.contins.synapse.data.local.converter.DataMapper.toEntity
import top.contins.synapse.domain.model.CalendarAccount
import top.contins.synapse.domain.repository.CalendarRepository
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val calendarDao: CalendarDao
) : CalendarRepository {

    override fun getAllCalendars(): Flow<List<CalendarAccount>> {
        return calendarDao.getAllCalendars().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCalendarById(id: String): CalendarAccount? {
        return calendarDao.getCalendarById(id)?.toDomain()
    }

    override suspend fun insertCalendar(calendar: CalendarAccount) {
        calendarDao.insertCalendar(calendar.toEntity())
    }

    override suspend fun updateCalendar(calendar: CalendarAccount) {
        calendarDao.updateCalendar(calendar.toEntity())
    }

    override suspend fun deleteCalendar(calendar: CalendarAccount) {
        calendarDao.deleteCalendar(calendar.toEntity())
    }

    override suspend fun deleteCalendarById(id: String) {
        calendarDao.deleteCalendarById(id)
    }
}
