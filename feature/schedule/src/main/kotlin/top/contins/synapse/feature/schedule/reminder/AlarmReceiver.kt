package top.contins.synapse.feature.schedule.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import top.contins.synapse.feature.schedule.service.ReminderGuardService
import javax.inject.Inject

import android.app.PendingIntent

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    private val tag = "ScheduleReminder";

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "收到广播: $intent")

        // 停止保活服务
        try {
            val serviceIntent = Intent(context, ReminderGuardService::class.java)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            Log.e(tag, "停止保活服务失败", e)
        }
        
        val scheduleId = intent.getStringExtra("schedule_id")
        if (scheduleId == null) {
            Log.e(tag, "日程ID为空")
            return
        }
        
        val title = intent.getStringExtra("title") ?: "日程提醒"
        val message = intent.getStringExtra("message") ?: ""
        val isAlarm = intent.getBooleanExtra("is_alarm", false)
        
        // 使用一致的 ID 生成策略进行通知更新
        val notificationId = scheduleId.hashCode()

        Log.d("ScheduleReminder", "接收到闹钟广播！标题: $title, 是否强提醒: $isAlarm")

        try {
            // 构建打开详情页的 Intent (MainActivity -> 通过导航逻辑处理日程详情 或 直接启动应用)
            // 目前根据类型直接启动应用主 Activity 或 闹钟全屏 Activity
            // 在实际应用中，可以使用 DeepLink。
            
            var pendingIntent: PendingIntent? = null
            
            if (isAlarm) {
                val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                    putExtra("title", title)
                    putExtra("message", message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                pendingIntent = PendingIntent.getActivity(
                    context,
                    notificationId, 
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                 // 对于普通通知，直接启动应用
                 // 理想情况下应该跳转到具体的日程详情
                 val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                 }
                 if (launchIntent != null) {
                     pendingIntent = PendingIntent.getActivity(
                        context,
                        notificationId,
                        launchIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                 }
            }

            notificationHelper.showNotification(notificationId, title, message, isAlarm, pendingIntent)

            if (isAlarm) {
                 // 尝试直接启动 Activity (针对前台场景或旧版 Android)
                try {
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra("title", title)
                        putExtra("message", message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(alarmIntent)
                } catch (e: Exception) {
                    Log.w("ScheduleReminder", "无法直接启动 Activity: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleReminder", "处理闹钟时发生错误", e)
        }
    }
}
