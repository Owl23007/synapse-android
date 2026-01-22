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
 * Service for syncing calendar subscriptions from network
 */
class SubscriptionSyncService @Inject constructor(
    private val iCalendarService: ICalendarService
) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Fetch iCalendar content from subscription URL
     * @param url URL to fetch from (supports http, https, webcal protocols)
     * @return iCalendar content as string
     * @throws IOException if network request fails
     */
    suspend fun fetchSubscriptionContent(url: String): String = withContext(Dispatchers.IO) {
        // Convert webcal:// to http:// for OkHttp
        val httpUrl = url.replace("webcal://", "http://")
        
        val request = Request.Builder()
            .url(httpUrl)
            .addHeader("User-Agent", "Synapse-Android/1.0")
            .addHeader("Accept", "text/calendar")
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw IOException("Failed to fetch subscription: ${response.code} ${response.message}")
        }
        
        response.body?.string() ?: throw IOException("Empty response body")
    }
    
    /**
     * Parse subscription content to schedules
     * @param icsContent iCalendar content
     * @param subscription Subscription info
     * @return List of parsed schedules
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
     * Validate subscription URL
     * @param url URL to validate
     * @return true if URL is valid and accessible
     */
    suspend fun validateSubscriptionUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val httpUrl = url.replace("webcal://", "http://")
            val request = Request.Builder()
                .url(httpUrl)
                .head() // Use HEAD request for validation
                .addHeader("User-Agent", "Synapse-Android/1.0")
                .build()
            
            val response = httpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
