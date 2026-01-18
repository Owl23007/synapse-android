package top.contins.synapse.feature.schedule.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import top.contins.synapse.domain.repository.ReminderManager
import top.contins.synapse.domain.repository.ScheduleRepository
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var reminderManager: ReminderManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(SupervisorJob())
            
            scope.launch {
                try {
                    // Reschedule active reminders
                    val schedules = scheduleRepository.getAllSchedules().first()
                    schedules.forEach { schedule ->
                        reminderManager.scheduleReminder(schedule)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
