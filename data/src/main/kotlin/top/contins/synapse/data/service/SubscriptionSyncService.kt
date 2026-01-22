package top.contins.synapse.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.contins.synapse.domain.model.schedule.Subscription
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 从网络同步日历订阅的服务
 * 
 * 注意：OkHttpClient 应该是单例，由 Hilt 管理其生命周期
 */
@Singleton
class SubscriptionSyncService @Inject constructor(
    private val iCalendarService: ICalendarService
) {
    
    companion object {
        private const val TAG = "SubscriptionSyncService"
        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val USER_AGENT = "Synapse-Android/1.0"
    }
    
    // 使用懒加载确保 HTTP 客户端只在需要时初始化
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * 从订阅 URL 获取 iCalendar 内容
     * @param url 要获取的 URL（支持 http、https、webcal 协议）
     * @return iCalendar 内容字符串
     * @throws IOException 网络请求失败时抛出
     * @throws IllegalArgumentException 如果 URL 为空或格式无效
     */
    suspend fun fetchSubscriptionContent(url: String): String = withContext(Dispatchers.IO) {
        require(url.isNotBlank()) { "订阅 URL 不能为空" }
        
        // 将 webcal:// 转换为 http:// 以便 OkHttp 处理
        val httpUrl = url.replace("webcal://", "http://")
        
        try {
            val request = Request.Builder()
                .url(httpUrl)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "text/calendar")
                .build()
            
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                throw IOException("获取订阅失败: HTTP ${response.code} ${response.message}")
            }
            
            response.body?.string() ?: throw IOException("响应体为空")
        } catch (e: IOException) {
            android.util.Log.e(TAG, "获取订阅内容失败: $url", e)
            throw e
        } catch (e: Exception) {
            android.util.Log.e(TAG, "获取订阅时发生未知错误: $url", e)
            throw IOException("获取订阅失败: ${e.message}", e)
        }
    }
    
    /**
     * 将订阅内容解析为日程
     * @param icsContent iCalendar 内容
     * @param subscription 订阅信息
     * @return 解析后的日程列表
     * @throws IllegalArgumentException 如果内容为空
     */
    fun parseSubscriptionContent(
        icsContent: String,
        subscription: Subscription
    ): List<top.contins.synapse.domain.model.schedule.Schedule> {
        require(icsContent.isNotBlank()) { "iCalendar 内容不能为空" }
        
        return iCalendarService.importFromICalendar(
            icsContent = icsContent,
            defaultCalendarId = subscription.id,
            subscriptionId = subscription.id
        )
    }
    
    /**
     * 验证订阅 URL 是否有效且可访问
     * @param url 要验证的 URL
     * @return 如果 URL 有效且可访问则返回 true
     */
    suspend fun validateSubscriptionUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        if (url.isBlank()) return@withContext false
        
        try {
            val httpUrl = url.replace("webcal://", "http://")
            val request = Request.Builder()
                .url(httpUrl)
                .head() // 使用 HEAD 请求进行验证，减少网络开销
                .addHeader("User-Agent", USER_AGENT)
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.w(TAG, "验证订阅 URL 失败: $url", e)
            false
        }
    }
    
    /**
     * 清理资源（如果需要）
     * 注意：由于 OkHttpClient 是单例且由 Hilt 管理，通常不需要手动清理
     */
    fun cleanup() {
        // OkHttpClient 会在应用结束时自动清理连接池
        // 如果需要强制清理，可以调用：
        // httpClient.dispatcher.executorService.shutdown()
        // httpClient.connectionPool.evictAll()
    }
}
