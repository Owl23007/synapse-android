package top.contins.synapse.domain.repository

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.domain.model.CalendarAccount

interface CalendarRepository {
    fun getAllCalendars(): Flow<List<CalendarAccount>>
    suspend fun getCalendarById(id: String): CalendarAccount?
    suspend fun insertCalendar(calendar: CalendarAccount)
    suspend fun updateCalendar(calendar: CalendarAccount)
    suspend fun deleteCalendar(calendar: CalendarAccount)
    suspend fun deleteCalendarById(id: String)
}
