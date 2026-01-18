package top.contins.synapse.feature.schedule.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import top.contins.synapse.feature.schedule.R

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_NORMAL = "schedule_normal_v2"
        const val CHANNEL_NAME_NORMAL = "日程提醒 (普通)"
        const val CHANNEL_ID_ALARM = "schedule_alarm_v2"
        const val CHANNEL_NAME_ALARM = "日程提醒 (强力)"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. 普通通道 (顶部悬浮)
        val normalChannel = NotificationChannel(
            CHANNEL_ID_NORMAL,
            CHANNEL_NAME_NORMAL,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "普通日程提醒"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(normalChannel)

        // 2. 强力通道 (闹钟)
        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            CHANNEL_NAME_ALARM,
            NotificationManager.IMPORTANCE_HIGH // MAX 不是有效的通道重要性常量，创建时 HIGH 为最大值
        ).apply {
            description = "强力闹钟提醒"
            enableVibration(true)
            enableLights(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI,
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        notificationManager.createNotificationChannel(alarmChannel)
    }

    fun showNotification(id: Int, title: String, content: String, isAlarm: Boolean, fullScreenIntent: PendingIntent? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("ScheduleReminder", "POST_NOTIFICATIONS 权限被拒绝！无法显示通知。")
                return
            }
        }

        Log.d("ScheduleReminder", "正在构建并显示通知: $title, 是否强提醒: $isAlarm")

        val channelId = if (isAlarm) CHANNEL_ID_ALARM else CHANNEL_ID_NORMAL
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            
        if (isAlarm) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Insistent: 持续响铃直到被处理 (需要 Notification.FLAG_INSISTENT 直接设置或通过 setOngoing 可能?)
            // setInsistent 在 NotificationCompat.Builder 中不直接提供，或者在特定版本。
            // 实际上 NotificationCompat.Builder 没有 setInsistent。必须使用 setDefaults 或手动添加 flag。
            // 这里我们稍后手动修改 notification 对象。
            
            builder.setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            builder.setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
            builder.setTimeoutAfter(10 * 60 * 1000) // 10分钟后自动取消
            
            if (fullScreenIntent != null) {
                // 对于闹钟，如果可能，我们希望同时拥有全屏 Intent 和点击打开的内容 Intent
                builder.setFullScreenIntent(fullScreenIntent, true)
                builder.setContentIntent(fullScreenIntent)
            }
        } else {
            builder.setCategory(NotificationCompat.CATEGORY_EVENT)
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
            // 如果需要点击打开应用，添加 ContentIntent
            if (fullScreenIntent != null) {
                 builder.setContentIntent(fullScreenIntent)
            }
        }

        val notification = builder.build()
        if (isAlarm) {
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
        }
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
