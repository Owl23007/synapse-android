package top.contins.synapse.feature.schedule.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.app.PendingIntent

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
            // Build Intent to open details (MainActivity -> Schedule Detail handled by Nav logic or just launch app)
            // For now, let's just launch the app main activity or the AlarmActivity depending on type
            // In a real app, you would use DeepLink.
            
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
                 // For normal notification, we can just launch the app
                 // Ideally this should point to the specific schedule detail
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
                 // Try to start activity directly as well (for Foreground case or older Android versions)
                try {
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra("title", title)
                        putExtra("message", message)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(alarmIntent)
                } catch (e: Exception) {
                    Log.w("ScheduleReminder", "Failed to start Activity directly (likely background restriction): ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleReminder", "Error handling alarm", e)
        }
    }
}
