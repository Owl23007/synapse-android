package top.contins.synapse.data.service

import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.property.*
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.model.schedule.ScheduleType
import top.contins.synapse.domain.model.schedule.RepeatRule
import java.util.*
import javax.inject.Inject

/**
 * Schedule 与 iCalendar 格式互转服务
 * 使用 biweekly 库处理 iCalendar（RFC 5545）的解析和生成
 */
class ICalendarService @Inject constructor() {
    
    companion object {
        private const val TAG = "ICalendarService"
        private const val ONE_HOUR_MS = 3600000L // 1小时的毫秒数
    }
    
    /**
     * 将日程列表转换为 iCalendar 格式字符串
     * @param schedules 要导出的日程列表
     * @return iCalendar 格式字符串（RFC 5545）
     * @throws IllegalArgumentException 如果日程列表为空
     */
    fun exportToICalendar(schedules: List<Schedule>): String {
        require(schedules.isNotEmpty()) { "日程列表不能为空" }
        
        val calendar = ICalendar()
        calendar.productId = ProductId("-//Synapse Android//Schedule Manager//EN")
        calendar.version = Version.v2_0()
        
        schedules.forEach { schedule ->
            try {
                val event = scheduleToVEvent(schedule)
                calendar.addEvent(event)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "转换日程失败: ${schedule.id}", e)
                // 继续处理其他日程
            }
        }
        
        return Biweekly.write(calendar).go()
    }
    
    /**
     * 解析 iCalendar 内容并转换为 Schedule 列表
     * @param icsContent iCalendar 格式字符串
     * @param defaultCalendarId 分配给导入日程的日历 ID
     * @param subscriptionId 可选的订阅 ID（如果从订阅导入）
     * @return 解析后的日程列表
     * @throws IllegalArgumentException 如果内容为空或 calendarId 无效
     */
    fun importFromICalendar(
        icsContent: String,
        defaultCalendarId: String,
        subscriptionId: String? = null
    ): List<Schedule> {
        require(icsContent.isNotBlank()) { "iCalendar 内容不能为空" }
        require(defaultCalendarId.isNotBlank()) { "日历 ID 不能为空" }
        
        val calendars = Biweekly.parse(icsContent).all()
        val schedules = mutableListOf<Schedule>()
        
        calendars.forEach { calendar ->
            calendar.events.forEach { event ->
                try {
                    val schedule = vEventToSchedule(
                        event,
                        defaultCalendarId,
                        subscriptionId
                    )
                    schedules.add(schedule)
                } catch (e: Exception) {
                    // 记录错误但继续处理其他事件
                    android.util.Log.e(TAG, "解析事件失败: ${event.uid?.value}", e)
                }
            }
        }
        
        return schedules
    }
    
    /**
     * 将 Schedule 转换为 VEvent
     */
    private fun scheduleToVEvent(schedule: Schedule): VEvent {
        val event = VEvent()
        
        // 基本属性
        event.summary = Summary(schedule.title)
        if (!schedule.description.isNullOrEmpty()) {
            event.description = Description(schedule.description)
        }
        
        // 时间属性
        val startDate = Date(schedule.startTime)
        val endDate = Date(schedule.endTime)
        
        if (schedule.isAllDay) {
            event.dateStart = DateStart(startDate, false)
            event.dateEnd = DateEnd(endDate, false)
        } else {
            event.dateStart = DateStart(startDate, true)
            event.dateEnd = DateEnd(endDate, true)
        }
        
        // 地点
        if (!schedule.location.isNullOrEmpty()) {
            event.location = Location(schedule.location)
        }
        
        // 唯一标识符
        event.uid = Uid(schedule.id)
        
        // 创建和修改时间
        event.created = Created(Date(schedule.createdAt))
        event.lastModified = LastModified(Date(schedule.updatedAt))
        
        // 分类（使用日程类型）
        event.addCategories(schedule.type.name)
        
        return event
    }
    
    /**
     * 将 VEvent 转换为 Schedule
     */
    private fun vEventToSchedule(
        event: VEvent,
        calendarId: String,
        subscriptionId: String?
    ): Schedule {
        val uid = event.uid?.value ?: UUID.randomUUID().toString()
        val title = event.summary?.value ?: "无标题事件"
        val description = event.description?.value
        
        val startTime = event.dateStart?.value?.time ?: System.currentTimeMillis()
        val endTime = event.dateEnd?.value?.time ?: (startTime + ONE_HOUR_MS)
        
        // 使用 biweekly 的 hasTime() 方法正确检测全天事件
        val isAllDay = event.dateStart?.let {
            it.hasTime() == false
        } ?: false
        
        val location = event.location?.value
        
        // 尝试从事件中获取时区信息，否则使用系统默认时区
        val timezoneId = event.dateStart?.parameters?.timezoneId?.value 
            ?: TimeZone.getDefault().id
        
        val now = System.currentTimeMillis()
        
        return Schedule(
            id = uid,
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            timezoneId = timezoneId,
            location = location,
            type = ScheduleType.EVENT, // 默认类型
            color = null,
            reminderMinutes = null,
            isAlarm = false,
            repeatRule = null, // TODO: 解析重复规则
            calendarId = calendarId,
            isAllDay = isAllDay,
            isFromSubscription = subscriptionId != null,
            subscriptionId = subscriptionId,
            createdAt = event.created?.value?.time ?: now,
            updatedAt = event.lastModified?.value?.time ?: now
        )
    }
}
