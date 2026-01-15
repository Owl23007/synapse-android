package top.contins.synapse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import top.contins.synapse.data.local.entity.chat.ConversationEntity
import top.contins.synapse.data.local.entity.chat.MessageEntity
import top.contins.synapse.data.local.dao.ChatDao

@Database(
    entities = [
        ScheduleEntity::class,
        CalendarEntity::class,
        SubscriptionEntity::class,
        TaskEntity::class,
        GoalEntity::class,
        ConversationEntity::class,
        MessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun calendarDao(): CalendarDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun taskDao(): TaskDao
    abstract fun goalDao(): GoalDao
    abstract fun chatDao(): ChatDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create conversations table
        database.execSQL("CREATE TABLE IF NOT EXISTS `conversations` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        
        // Create messages table
        database.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `content` TEXT NOT NULL, `role` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
        
        // Create index for messages
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)")
    }
}
