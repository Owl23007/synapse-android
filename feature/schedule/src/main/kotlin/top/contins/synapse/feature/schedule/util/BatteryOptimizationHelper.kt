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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }

    /**
     * 请求忽略电池优化
     * 注意：这通常会触发一个系统弹窗，或者在该函数被调用后需要通过 Intent 跳转到设置页。
     * 由于 Google Play 政策限制，通常建议使用 ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS 跳转到列表让用户手动选。
     * 但如果是针对特定高可靠性需求场景且符合政策（如闹钟应用），可以使用 ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 直接弹窗。
     */
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(onStartActivity: (Intent) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations()) {
                try {
                    // 尝试直接弹窗请求（需要 REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 权限）
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    onStartActivity(intent)
                } catch (e: Exception) {
                    // 如果直接请求失败（如权限被拒或ROM限制），则跳转到设置列表
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    onStartActivity(intent)
                }
            }
        }
    }
}
