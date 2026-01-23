package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.service.ICalendarService
import javax.inject.Inject

/**
 * 导入操作结果
 */
data class ImportResult(
    val successCount: Int,      // 成功导入数量
    val failedCount: Int,        // 导入失败数量
    val conflicts: List<Schedule>,  // 冲突的日程列表
    val imported: List<Schedule>    // 已导入的日程列表
)

/**
 * 导入时处理冲突日程的策略
 */
enum class ConflictStrategy {
    SKIP,       // 跳过冲突的日程
    REPLACE,    // 替换已存在的日程
    KEEP_BOTH   // 保留两个日程
}

/**
 * 从 iCalendar 格式导入日程
 * 
 * 说明：实际的 iCalendar 解析将由数据层的 ICalendarService 完成
 * 此用例负责协调导入操作
 * 
 * 注意：当前实现为占位符，需要在数据层集成 ICalendarService 后完成
 * 实际使用时需要通过依赖注入添加 ICalendarService
 */
class ImportScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository,
    private val iCalendarService: ICalendarService
) {
    /**
     * 从 iCalendar 格式字符串导入日程
     * @param icsContent iCalendar 格式字符串（RFC 5545）
     * @param calendarId 目标日历 ID
     * @param handleConflicts 处理冲突的策略（跳过、替换、保留两者）
     * @return 导入结果，包含成功/失败计数和冲突信息
     * @throws IllegalArgumentException 如果 icsContent 为空或 calendarId 无效
     */
    suspend operator fun invoke(
        icsContent: String,
        calendarId: String,
        handleConflicts: ConflictStrategy = ConflictStrategy.SKIP
    ): ImportResult {
        require(icsContent.isNotBlank()) { "iCalendar 内容不能为空" }
        require(calendarId.isNotBlank()) { "日历 ID 不能为空" }
        
        val parsedSchedules = iCalendarService.importFromICalendar(icsContent, calendarId)
        
        val imported = mutableListOf<Schedule>()
        val conflicts = mutableListOf<Schedule>()
        var successCount = 0
        var failedCount = 0
        
        parsedSchedules.forEach { schedule ->
            try {
                // 检查时间冲突
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
                android.util.Log.e("ImportScheduleUseCase", "导入日程失败: ${schedule.id}", e)
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
