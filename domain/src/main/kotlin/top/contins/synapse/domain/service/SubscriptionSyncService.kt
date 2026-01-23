package top.contins.synapse.domain.service

import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.model.schedule.Schedule

/**
 * 订阅同步服务接口
 */
interface SubscriptionSyncService {
    /**
     * 从订阅 URL 获取 iCalendar 内容
     */
    suspend fun fetchSubscriptionContent(url: String): String

    /**
     * 将订阅内容解析为日程列表
     */
    fun parseSubscriptionContent(icsContent: String, subscription: Subscription): List<Schedule>

    /**
     * 验证订阅 URL 是否有效且可访问
     */
    suspend fun validateSubscriptionUrl(url: String): Boolean
}
