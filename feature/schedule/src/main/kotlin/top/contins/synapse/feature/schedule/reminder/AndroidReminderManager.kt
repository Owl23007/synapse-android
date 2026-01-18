package top.contins.synapse.feature.schedule.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import top.contins.synapse.domain.model.schedule.Schedule
import top.contins.synapse.domain.repository.ReminderManager
import top.contins.synapse.feature.schedule.service.ReminderGuardService
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
            val timeUntilTrigger = triggerTime - System.currentTimeMillis()

            if (timeUntilTrigger > 0) {
                // Layer 3: 临近提醒时（<= 10分钟），启动前台服务保活
                if (timeUntilTrigger <= 10 * 60 * 1000) {
                    startGuardService()
                }

                val pendingIntent = createPendingIntent(schedule, minutes)

                Log.d("ScheduleReminder", "正在调度日程提醒: ${schedule.title} 时间: $triggerTime (提前: $minutes 分钟)")

                // Layer 1: 使用 setAlarmClock (强力) 或 setExactAndAllowWhileIdle (普通)
                try {
                    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        alarmManager.canScheduleExactAlarms()
                    } else {
                        true
                    }

                    if (canScheduleExact) {
                        if (schedule.isAlarm) {
                            // AlarmClock 具有最高优先级，能唤醒设备并显示闹钟图标
                            val alarmInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)
                            alarmManager.setAlarmClock(alarmInfo, pendingIntent)
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                            )
                        }
                    } else {
                         Log.w("ScheduleReminder", "无法调度精确闹钟，降级为非精确闹钟")
                         alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } catch (e: SecurityException) {
                    Log.e("ScheduleReminder", "调度闹钟时发生 SecurityException！可能缺少权限？", e)
                } catch (e: Exception) {
                    Log.e("ScheduleReminder", "调度闹钟错误", e)
                }
            } else {
                Log.d("ScheduleReminder", "跳过过期提醒: ${schedule.title} (触发时间: $triggerTime, 当前: ${System.currentTimeMillis()})")
            }
        }
    }

    private fun startGuardService() {
        try {
            val intent = Intent(context, ReminderGuardService::class.java)
            context.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e("ScheduleReminder", "无法启动 ReminderGuardService", e)
        }
    }

    override suspend fun cancelReminder(schedule: Schedule) {
        val reminders = schedule.reminderMinutes ?: return
        reminders.forEach { minutes ->
            val pendingIntent = createPendingIntent(schedule, minutes)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ScheduleReminder", "已取消日程提醒: ${schedule.title} (分钟: $minutes)")
        }
    }

    private fun createPendingIntent(schedule: Schedule, minutes: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("title", schedule.title)
            putExtra("message", schedule.location ?: "即将开始")
            putExtra("is_alarm", schedule.isAlarm)
            // 区分不同的 Action 防止 Intent 冲突
            action = "top.contins.synapse.REMINDER_${schedule.id}_$minutes"
        }
        
        // 唯一请求码: scheduleId.hashCode() + minutes 避免冲突
        val requestCode = (schedule.id + "_$minutes").hashCode()

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
