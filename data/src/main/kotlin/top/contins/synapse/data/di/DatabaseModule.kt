package top.contins.synapse.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.data.local.AppDatabase
import top.contins.synapse.data.local.dao.CalendarDao
import top.contins.synapse.data.local.dao.GoalDao
import top.contins.synapse.data.local.dao.ScheduleDao
import top.contins.synapse.data.local.dao.SubscriptionDao
import top.contins.synapse.data.local.dao.TaskDao
import javax.inject.Singleton

import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "synapse_db"
        )
        .addMigrations(top.contins.synapse.data.local.MIGRATION_3_4)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()
                db.execSQL("INSERT OR IGNORE INTO calendars (id, name, color, is_visible, is_default, created_at, updated_at) VALUES ('default', '默认日历', 4280391411, 1, 1, $now, $now)")
            }
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val now = System.currentTimeMillis()
                db.execSQL("INSERT OR IGNORE INTO calendars (id, name, color, is_visible, is_default, created_at, updated_at) VALUES ('default', '默认日历', 4280391411, 1, 1, $now, $now)")
            }
        })
        .fallbackToDestructiveMigration() // 如果数据库结构发生变化，则删除旧数据库并重新创建
        .build()
    }

    @Provides
    @Singleton
    fun provideScheduleDao(database: AppDatabase): ScheduleDao {
        return database.scheduleDao()
    }

    @Provides
    @Singleton
    fun provideCalendarDao(database: AppDatabase): CalendarDao {
        return database.calendarDao()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(database: AppDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): top.contins.synapse.data.local.dao.ChatDao {
        return database.chatDao()
    }
}
