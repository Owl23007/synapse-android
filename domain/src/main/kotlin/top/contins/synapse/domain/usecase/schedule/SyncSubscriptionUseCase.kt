package top.contins.synapse.domain.usecase.schedule

import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.SubscriptionRepository
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
 */
class SyncSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * 同步单个订阅
     * @param subscriptionId 要同步的订阅 ID
     * @return 同步结果，包含统计信息
     */
    suspend operator fun invoke(subscriptionId: String): SyncResult {
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
            
            // TODO: 集成 SubscriptionSyncService
            // 当前返回占位符结果
            // 实际实现将：
            // 1. 使用 SubscriptionSyncService.fetchSubscriptionContent(url)
            // 2. 使用 ICalendarService.importFromICalendar() 解析
            // 3. 删除旧日程并插入新日程
            // 4. 更新 lastSyncAt 时间戳
            
            // 更新最后同步时间
            val updatedSubscription = subscription.copy(
                lastSyncAt = System.currentTimeMillis()
            )
            subscriptionRepository.updateSubscription(updatedSubscription)
            
            SyncResult(
                success = true,
                addedCount = 0,
                updatedCount = 0,
                removedCount = 0,
                error = null
            )
        } catch (e: Exception) {
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
     */
    suspend fun syncAll(): Map<String, SyncResult> {
        val results = mutableMapOf<String, SyncResult>()
        // TODO: 实现所有已启用订阅的 Flow 收集
        return results
    }
}
