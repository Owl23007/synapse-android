package top.contins.synapse.domain.usecase.subscription

import top.contins.synapse.domain.model.schedule.CalendarAccount
import top.contins.synapse.domain.model.schedule.Subscription
import top.contins.synapse.domain.repository.CalendarRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import javax.inject.Inject
import java.util.UUID

/**
 * 创建新的日历订阅
 */
class CreateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
    private val calendarRepository: CalendarRepository
) {
    /**
     * 创建新订阅
     * @param name 订阅显示名称
     * @param url iCalendar 订阅源 URL
     * @param color 此订阅事件的可选颜色
     * @param syncInterval 同步间隔（小时），默认 24 小时
     * @return 创建的订阅 ID
     */
    suspend operator fun invoke(
        name: String,
        url: String,
        color: Long? = null,
        syncInterval: Int = 24
    ): String {
        require(name.isNotBlank()) { "订阅名称不能为空" }
        require(url.isNotBlank()) { "订阅 URL 不能为空" }
        require(syncInterval > 0) { "同步间隔必须大于 0" }
        
        // 验证 URL 格式
        require(isValidUrl(url)) { "URL 格式无效" }
        
        val subscription = Subscription(
            id = UUID.randomUUID().toString(),
            name = name,
            url = url,
            color = color,
            syncInterval = syncInterval,
            lastSyncAt = null,
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )
        
        repository.insertSubscription(subscription)
        calendarRepository.insertCalendar(
            CalendarAccount(
                id = subscription.id,
                name = subscription.name,
                color = subscription.color ?: DEFAULT_SUBSCRIPTION_COLOR,
                isVisible = true,
                isDefault = false,
                defaultReminderMinutes = null,
                createdAt = subscription.createdAt,
                updatedAt = subscription.createdAt
            )
        )
        
        return subscription.id
    }
    
    /**
     * 验证 URL 是否有效
     */
    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            uri.scheme in listOf("http", "https", "webcal")
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val DEFAULT_SUBSCRIPTION_COLOR: Long = 4280391411
    }
}
