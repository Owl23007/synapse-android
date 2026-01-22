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
 * Strategy for handling conflicting schedules during import
 */
enum class ConflictStrategy {
    SKIP,       // Skip conflicting schedules
    REPLACE,    // Replace existing schedules
    KEEP_BOTH   // Keep both schedules
}

/**
 * Import schedules from iCalendar format
 * 
 * Note: The actual iCalendar parsing will be done by ICalendarService
 * in the data layer. This use case coordinates the import operation.
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
        // TODO: Integrate with ICalendarService.importFromICalendar()
        // For now, return a placeholder result
        // The actual implementation will:
        // 1. Use ICalendarService.importFromICalendar(icsContent, calendarId)
        // 2. Check for conflicts using repository.getConflictingSchedules()
        // 3. Handle conflicts based on strategy
        // 4. Insert schedules into repository
        
        val parsedSchedules = emptyList<Schedule>() // Placeholder
        
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
}
