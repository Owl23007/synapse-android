package top.contins.synapse.domain.usecase.schedule

import kotlinx.coroutines.flow.first
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.service.ICalendarService
import javax.inject.Inject

/**
 * 导出日程到 iCalendar 格式
 * 
 * 说明：实际的 iCalendar 转换将由数据层的 ICalendarService 完成
 * 此用例负责协调导出操作
 * 
 * 注意：当前实现为占位符，需要在数据层集成 ICalendarService 后完成
 * 实际使用时需要通过依赖注入添加 ICalendarService
 */
class ExportScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository,
    private val iCalendarService: ICalendarService
) {
    companion object {
        private const val EMPTY_CALENDAR = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//Synapse Android//Schedule Manager//EN\nEND:VCALENDAR"
    }
    
    /**
     * 导出日程为 iCalendar 格式字符串
     * @param scheduleIds 要导出的日程 ID 列表
     * @return iCalendar 格式字符串（RFC 5545）
     * @throws IllegalArgumentException 如果 scheduleIds 为空
     */
    suspend operator fun invoke(scheduleIds: List<String>): String {
        require(scheduleIds.isNotEmpty()) { "日程 ID 列表不能为空" }
        
        val schedules = scheduleIds.mapNotNull { repository.getScheduleById(it) }
        
        if (schedules.isEmpty()) {
            return EMPTY_CALENDAR
        }
        
        return iCalendarService.exportToICalendar(schedules)
    }
    
    /**
     * 导出指定时间范围内的日程
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return iCalendar 格式字符串（RFC 5545）
     * @throws IllegalArgumentException 如果时间范围无效
     */
    suspend fun exportTimeRange(startTime: Long, endTime: Long): String {
        require(startTime < endTime) { "开始时间必须早于结束时间" }
        
        val schedules = repository.getSchedulesInTimeRange(startTime, endTime).first()
        if (schedules.isEmpty()) {
            return EMPTY_CALENDAR
        }
        return iCalendarService.exportToICalendar(schedules)
    }
}
