package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

/**
 * Result of import operation
 */
data class ImportResult(
    val successCount: Int,
    val failedCount: Int,
    val conflicts: List<Schedule>,
    val imported: List<Schedule>
)

/**
 * Import schedules from iCalendar format
 */
class ImportScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Import schedules from iCalendar format string
     * @param icsContent iCalendar format string (RFC 5545)
     * @param calendarId Target calendar ID to import into
     * @param handleConflicts Strategy to handle conflicts (skip, replace, keep both)
     * @return Import result with success/failure counts and conflicts
     */
    suspend operator fun invoke(
        icsContent: String,
        calendarId: String,
        handleConflicts: ConflictStrategy = ConflictStrategy.SKIP
    ): ImportResult {
        // Parse iCalendar content (will be done by ICalendarService)
        val parsedSchedules = parseICalendar(icsContent, calendarId)
        
        val imported = mutableListOf<Schedule>()
        val conflicts = mutableListOf<Schedule>()
        var successCount = 0
        var failedCount = 0
        
        parsedSchedules.forEach { schedule ->
            try {
                // Check for conflicts
                val conflicting = repository.getConflictingSchedules(
                    schedule.startTime,
                    schedule.endTime
                )
                
                if (conflicting.isNotEmpty()) {
                    conflicts.add(schedule)
                    when (handleConflicts) {
                        ConflictStrategy.SKIP -> failedCount++
                        ConflictStrategy.REPLACE -> {
                            conflicting.forEach { repository.deleteSchedule(it) }
                            repository.insertSchedule(schedule)
                            imported.add(schedule)
                            successCount++
                        }
                        ConflictStrategy.KEEP_BOTH -> {
                            repository.insertSchedule(schedule)
                            imported.add(schedule)
                            successCount++
                        }
                    }
                } else {
                    repository.insertSchedule(schedule)
                    imported.add(schedule)
                    successCount++
                }
            } catch (e: Exception) {
                failedCount++
            }
        }
        
        return ImportResult(
            successCount = successCount,
            failedCount = failedCount,
            conflicts = conflicts,
            imported = imported
        )
    }
    
    private fun parseICalendar(icsContent: String, calendarId: String): List<Schedule> {
        // This will be implemented with actual biweekly library parsing
        // For now, return empty list as placeholder
        return emptyList()
    }
}

enum class ConflictStrategy {
    SKIP,       // Skip conflicting schedules
    REPLACE,    // Replace existing schedules
    KEEP_BOTH   // Keep both schedules
}
