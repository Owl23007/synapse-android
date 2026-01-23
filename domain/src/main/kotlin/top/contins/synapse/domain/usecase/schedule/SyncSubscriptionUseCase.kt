package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.model.schedule.CalendarAccount
import top.contins.synapse.domain.repository.CalendarRepository
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
import top.contins.synapse.domain.service.SubscriptionSyncService
import javax.inject.Inject

/**
 * 订阅同步操作结果
 */
data class SyncResult(
    val success: Boolean,       // 是否成功
    val addedCount: Int,        // 新增日程数量
    val updatedCount: Int,      // 更新日程数量
    val removedCount: Int,      // 删除日程数量
    val error: String? = null   // 错误信息
)

/**
 * 从网络同步日历订阅
 * 
 * 说明：此用例将与数据层的同步服务集成实现
 * 同步逻辑由 SubscriptionSyncService 和 ICalendarService 处理
 * 
 * 注意：当前实现为占位符，需要在数据层集成服务后完成
 * 实际使用时需要通过依赖注入添加 SubscriptionSyncService
 */
class SyncSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val calendarRepository: CalendarRepository,
    private val subscriptionSyncService: SubscriptionSyncService
) {
    /**
     * 同步单个订阅
     * @param subscriptionId 要同步的订阅 ID
     * @return 同步结果，包含统计信息
     * @throws IllegalArgumentException 如果订阅 ID 无效
     */
    suspend operator fun invoke(subscriptionId: String): SyncResult {
        require(subscriptionId.isNotBlank()) { "订阅 ID 不能为空" }
        
        return try {
            val subscription = subscriptionRepository.getSubscriptionById(subscriptionId)
                ?: return SyncResult(
                    success = false,
                    addedCount = 0,
                    updatedCount = 0,
                    removedCount = 0,
                    error = "订阅不存在"
                )
            
            if (!subscription.isEnabled) {
                return SyncResult(
                    success = false,
                    addedCount = 0,
                    updatedCount = 0,
                    removedCount = 0,
                    error = "订阅已禁用"
                )
            }
            
            // 确保订阅对应的日历存在（兼容历史数据）
            val existingCalendar = calendarRepository.getCalendarById(subscription.id)
            if (existingCalendar == null) {
                calendarRepository.insertCalendar(
                    CalendarAccount(
                        id = subscription.id,
                        name = subscription.name,
                        color = subscription.color ?: DEFAULT_SUBSCRIPTION_COLOR,
                        isVisible = true,
                        isDefault = false,
                        defaultReminderMinutes = null,
                        createdAt = subscription.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            val icsContent = subscriptionSyncService.fetchSubscriptionContent(subscription.url)
            val schedules = subscriptionSyncService.parseSubscriptionContent(icsContent, subscription)

            scheduleRepository.deleteSchedulesByCalendarId(subscription.id)
            schedules.forEach { scheduleRepository.insertSchedule(it) }
            
            // 更新最后同步时间
            val updatedSubscription = subscription.copy(
                lastSyncAt = System.currentTimeMillis()
            )
            subscriptionRepository.updateSubscription(updatedSubscription)
            
            SyncResult(
                success = true,
                addedCount = schedules.size,
                updatedCount = 0,
                removedCount = 0,
                error = null
            )
        } catch (e: Exception) {
            android.util.Log.e("SyncSubscriptionUseCase", "同步订阅失败: $subscriptionId", e)
            SyncResult(
                success = false,
                addedCount = 0,
                updatedCount = 0,
                removedCount = 0,
                error = e.message ?: "未知错误"
            )
        }
    }
    
    /**
     * 同步所有已启用的订阅
     * @return 每个订阅 ID 对应的同步结果
     */
    suspend fun syncAll(): Map<String, SyncResult> {
        val results = mutableMapOf<String, SyncResult>()
        // TODO: 实现所有已启用订阅的 Flow 收集
        // 实际实现：
        // subscriptionRepository.getAllSubscriptions().first()
        //     .filter { it.isEnabled }
        //     .forEach { subscription ->
        //         results[subscription.id] = invoke(subscription.id)
        //     }
        return results
    }

    companion object {
        private const val DEFAULT_SUBSCRIPTION_COLOR: Long = 4280391411
    }
}
