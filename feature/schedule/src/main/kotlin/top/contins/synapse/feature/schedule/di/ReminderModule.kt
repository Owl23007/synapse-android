package top.contins.synapse.feature.schedule.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.domain.repository.ReminderManager
import top.contins.synapse.feature.schedule.reminder.AndroidReminderManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindReminderManager(
        impl: AndroidReminderManager
    ): ReminderManager
}
