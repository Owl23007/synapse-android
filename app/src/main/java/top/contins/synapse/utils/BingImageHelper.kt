package top.contins.synapse.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

/**
 * 必应每日一图帮助类
 * 提供图片URL获取和本地缓存功能
 */
object BingImageHelper {
    private const val TAG = "BingImageHelper"
    private const val BING_API = "https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1"
    private const val BING_BASE_URL = "https://www.bing.com"
    private const val CACHE_FILE_NAME = "bing_image_cache.txt"
    
    /**
     * 获取今日必应图片URL
     * 优先从缓存读取，如果缓存过期则从网络获取
     */
    suspend fun getTodayImageUrl(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                val cacheFile = File(context.cacheDir, CACHE_FILE_NAME)
                
                // 检查缓存是否有效
                if (cacheFile.exists()) {
                    val cacheContent = cacheFile.readText()
                    val lines = cacheContent.split("\n")
                    if (lines.size >= 2) {
                        val cacheDate = lines[0]
                        val cacheUrl = lines[1]
                        
                        // 如果是今天的缓存，直接返回
                        if (cacheDate == today && cacheUrl.isNotEmpty()) {
                            Log.d(TAG, "使用缓存的图片URL: $cacheUrl")
                            return@withContext cacheUrl
                        }
                    }
                }
                
                // 缓存无效，从网络获取
                Log.d(TAG, "从网络获取必应图片")
                val imageUrl = fetchImageUrlFromNetwork()
                
                // 保存到缓存
                if (imageUrl != null) {
                    cacheFile.writeText("$today\n$imageUrl")
                    Log.d(TAG, "已缓存图片URL: $imageUrl")
                }
                
                imageUrl
            } catch (e: Exception) {
                Log.e(TAG, "获取必应图片失败", e)
                null
            }
        }
    }
    
    /**
     * 从必应API获取图片URL
     */
    private fun fetchImageUrlFromNetwork(): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(BING_API)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val images = jsonObject.getJSONArray("images")
                
                if (images.length() > 0) {
                    val imageObject = images.getJSONObject(0)
                    val urlPath = imageObject.getString("url")
                    return "$BING_BASE_URL$urlPath"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "网络请求失败", e)
        } finally {
            connection?.disconnect()
        }
        return null
    }
    
    /**
     * 清除缓存
     */
    fun clearCache(context: Context) {
        val cacheFile = File(context.cacheDir, CACHE_FILE_NAME)
        if (cacheFile.exists()) {
            cacheFile.delete()
            Log.d(TAG, "已清除缓存")
        }
    }
}
