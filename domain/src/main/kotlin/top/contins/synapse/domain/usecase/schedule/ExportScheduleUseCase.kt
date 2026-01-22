package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

/**
 * Export schedules to iCalendar format
 */
class ExportScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Export schedules to iCalendar format string
     * @param scheduleIds List of schedule IDs to export, null means export all
     * @return iCalendar format string (RFC 5545)
     */
    suspend operator fun invoke(scheduleIds: List<String>? = null): String {
        val schedules = if (scheduleIds != null) {
            scheduleIds.mapNotNull { repository.getScheduleById(it) }
        } else {
            // For export all, we need to implement in repository
            // For now, return empty as we don't have a way to get all schedules synchronously
            emptyList()
        }
        
        // The actual conversion will be done by ICalendarService in data layer
        // This use case will coordinate the operation
        return schedules.joinToString("\n") { 
            "SCHEDULE: ${it.title}" 
        }
    }
    
    /**
     * Export schedules in a time range
     */
    suspend fun exportTimeRange(startTime: Long, endTime: Long): String {
        // Will be implemented with actual iCalendar conversion
        return "VCALENDAR:2.0"
    }
}
