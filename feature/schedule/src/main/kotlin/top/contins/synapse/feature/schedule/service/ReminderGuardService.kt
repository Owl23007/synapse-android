package top.contins.synapse.feature.schedule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import top.contins.synapse.feature.schedule.R

/**
 * 提醒守护服务
 * 用于在提醒即将触发时（<=10分钟）启动前台服务，防止应用进程被系统杀除。
 * 这是一个短时运行的服务，触发提醒后立即停止
 */
class ReminderGuardService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 服务启动后保持运行，直到显式 stopService
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "schedule_guard_service"
        val channelName = "提醒守护服务"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "确保日程提醒准时触发"
            setShowBadge(false)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("日程提醒服务运行中")
            .setContentText("正在确保护即将到来的提醒准时触发...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 2001
    }
}
