package top.contins.synapse.feature.schedule.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

/**
 * 电池优化检查帮助类
 * 用于检测并引导用户关闭 Doze 模式（电池优化），确保后台任务不被杀。
 */
@Singleton
class BatteryOptimizationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * 检查当前应用是否已被忽略电池优化（即处于白名单中）
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 请求忽略电池优化
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(onStartActivity: (Intent) -> Unit) {
        if (!isIgnoringBatteryOptimizations()) {
            try {
                // 尝试直接弹窗请求（需要 REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 权限）
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                onStartActivity(intent)
            } catch ( _: Exception) {
                // 如果直接请求失败（如权限被拒或ROM限制），则跳转到设置列表
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                onStartActivity(intent)
            }
        }
    }
}
