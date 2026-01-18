package top.contins.synapse.feature.schedule.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ReminderManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class AndroidReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderManager {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun scheduleReminder(schedule: Schedule) {
        val reminders = schedule.reminderMinutes ?: return
        if (reminders.isEmpty()) return

        val scheduleId = schedule.id
        val scheduleTime = schedule.startTime

        reminders.forEach { minutes ->
            val triggerTime = scheduleTime - TimeUnit.MINUTES.toMillis(minutes.toLong())
            if (triggerTime > System.currentTimeMillis()) {
                val pendingIntent = createPendingIntent(schedule, minutes)

                Log.d("ScheduleReminder", "Scheduling reminder for schedule: ${schedule.title} at $triggerTime (minutes: $minutes)")

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        } else {
                             Log.w("ScheduleReminder", "Cannot schedule exact alarm, falling back to inexact")
                             alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    } else {
                         alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    Log.e("ScheduleReminder", "SecurityException when scheduling alarm! Missing permission?", e)
                } catch (e: Exception) {
                    Log.e("ScheduleReminder", "Error scheduling alarm", e)
                }
            } else {
                Log.d("ScheduleReminder", "Skipping past reminder for schedule: ${schedule.title} (triggerTime: $triggerTime, now: ${System.currentTimeMillis()})")
            }
        }
    }

    override suspend fun cancelReminder(schedule: Schedule) {
        val reminders = schedule.reminderMinutes ?: return
        reminders.forEach { minutes ->
            val pendingIntent = createPendingIntent(schedule, minutes)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ScheduleReminder", "Cancelled reminder for schedule: ${schedule.title} (minutes: $minutes)")
        }
    }

    private fun createPendingIntent(schedule: Schedule, minutes: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("title", schedule.title)
            putExtra("message", schedule.location ?: "即将开始")
            // Distinct action just in case
            action = "top.contins.synapse.REMINDER_${schedule.id}_$minutes"
        }
        
        // Unique Request Code: scheduleId.hashCode() + minutes to avoid collision
        val requestCode = (schedule.id + "_$minutes").hashCode()

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
