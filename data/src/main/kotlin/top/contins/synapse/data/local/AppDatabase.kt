package top.contins.synapse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import top.contins.synapse.data.local.dao.CalendarDao
import top.contins.synapse.data.local.dao.GoalDao
import top.contins.synapse.data.local.dao.ScheduleDao
import top.contins.synapse.data.local.dao.SubscriptionDao
import top.contins.synapse.data.local.dao.TaskDao
import top.contins.synapse.data.local.entity.CalendarEntity
import top.contins.synapse.data.local.entity.GoalEntity
import top.contins.synapse.data.local.entity.ScheduleEntity
import top.contins.synapse.data.local.entity.SubscriptionEntity
import top.contins.synapse.data.local.entity.TaskEntity

@Database(
    entities = [
        ScheduleEntity::class,
        CalendarEntity::class,
        SubscriptionEntity::class,
        TaskEntity::class,
        GoalEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun calendarDao(): CalendarDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
}
