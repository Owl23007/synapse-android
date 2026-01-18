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
        const val CHANNEL_ID = "schedule_reminders"
        const val CHANNEL_NAME = "日程提醒"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "显示日程到期提醒"
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
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showNotification(id: Int, title: String, content: String, fullScreenIntent: PendingIntent? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("ScheduleReminder", "POST_NOTIFICATIONS permission denied! Cannot show notification.")
                return
            }
        }

        Log.d("ScheduleReminder", "Building and showing notification: $title")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            
        if (fullScreenIntent != null) {
            builder.setFullScreenIntent(fullScreenIntent, true)
            builder.setPriority(NotificationCompat.PRIORITY_MAX) // Max priority for alarm
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Ensure sound/vibrate plays even if channel settings are slightly off (compat)
            builder.setVibrate(longArrayOf(0, 1000, 500, 1000))
            builder.setSound(android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
