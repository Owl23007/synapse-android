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
        // Use a consistent ID generation strategy for notification updates
        val notificationId = scheduleId.hashCode()

        Log.d("ScheduleReminder", "Alarm Received! Showing notification for: $title (ID: $scheduleId)")
        
        try {
            notificationHelper.showNotification(notificationId, title, message)
        } catch (e: Exception) {
            Log.e("ScheduleReminder", "Error showing notification", e)
        }
    }
}
