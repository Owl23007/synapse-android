package top.contins.synapse.feature.schedule.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScheduleReminder", "AlarmReceiver.onReceive called with intent: $intent")
        
        val scheduleId = intent.getStringExtra("schedule_id")
        if (scheduleId == null) {
            Log.e("ScheduleReminder", "AlarmReceiver: schedule_id is NULL! Aborting.")
            return
        }
        
        val title = intent.getStringExtra("title") ?: "日程提醒"
        val message = intent.getStringExtra("message") ?: ""
        val isAlarm = intent.getBooleanExtra("is_alarm", false)
        
        // Use a consistent ID generation strategy for notification updates
        val notificationId = scheduleId.hashCode()

        Log.d("ScheduleReminder", "Alarm Received! Title: $title, IsAlarm: $isAlarm")

        try {
            if (isAlarm) {
                val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                    putExtra("title", title)
                    putExtra("message", message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(alarmIntent)
                // Also show a notification as a fallback or history? Maybe not needed for full screen alarm.
            } else {
                notificationHelper.showNotification(notificationId, title, message)
            }
        } catch (e: Exception) {
            Log.e("ScheduleReminder", "Error handling alarm", e)
        }
    }
}
