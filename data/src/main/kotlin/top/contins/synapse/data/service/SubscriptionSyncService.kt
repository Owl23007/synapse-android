package top.contins.synapse.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.contins.synapse.domain.model.schedule.Subscription
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 从网络同步日历订阅的服务
 */
class SubscriptionSyncService @Inject constructor(
    private val iCalendarService: ICalendarService
) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 从订阅 URL 获取 iCalendar 内容
     * @param url 要获取的 URL（支持 http、https、webcal 协议）
     * @return iCalendar 内容字符串
     * @throws IOException 网络请求失败时抛出
     */
    suspend fun fetchSubscriptionContent(url: String): String = withContext(Dispatchers.IO) {
        // 将 webcal:// 转换为 http:// 以便 OkHttp 处理
        val httpUrl = url.replace("webcal://", "http://")
        
        val request = Request.Builder()
            .url(httpUrl)
            .addHeader("User-Agent", "Synapse-Android/1.0")
            .addHeader("Accept", "text/calendar")
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("获取订阅失败: ${response.code} ${response.message}")
        }
        
        response.body?.string() ?: throw IOException("响应体为空")
    }
    
    /**
     * 将订阅内容解析为日程
     * @param icsContent iCalendar 内容
     * @param subscription 订阅信息
     * @return 解析后的日程列表
     */
    fun parseSubscriptionContent(
        icsContent: String,
        subscription: Subscription
    ): List<top.contins.synapse.domain.model.schedule.Schedule> {
        return iCalendarService.importFromICalendar(
            icsContent = icsContent,
            defaultCalendarId = subscription.id,
            subscriptionId = subscription.id
        )
    }
    
    /**
     * 验证订阅 URL
     * @param url 要验证的 URL
     * @return 如果 URL 有效且可访问则返回 true
     */
    suspend fun validateSubscriptionUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val httpUrl = url.replace("webcal://", "http://")
            val request = Request.Builder()
                .url(httpUrl)
                .head() // 使用 HEAD 请求进行验证
                .addHeader("User-Agent", "Synapse-Android/1.0")
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
