package top.contins.synapse.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.data.local.AppDatabase
import top.contins.synapse.data.local.dao.CalendarDao
import top.contins.synapse.data.local.dao.ScheduleDao
import top.contins.synapse.data.local.dao.SubscriptionDao
import javax.inject.Singleton

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
        ).build()
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
}
