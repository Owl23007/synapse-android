package top.contins.synapse.domain.service

import top.contins.synapse.domain.model.schedule.Schedule

/**
 * iCalendar 转换服务接口
 */
interface ICalendarService {
    /**
     * 将日程列表转换为 iCalendar 格式字符串
     */
    fun exportToICalendar(schedules: List<Schedule>): String

    /**
     * 解析 iCalendar 内容并转换为 Schedule 列表
     */
    fun importFromICalendar(
        icsContent: String,
        defaultCalendarId: String,
        subscriptionId: String? = null
    ): List<Schedule>
}
