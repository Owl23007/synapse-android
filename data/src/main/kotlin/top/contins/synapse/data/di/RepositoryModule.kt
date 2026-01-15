package top.contins.synapse.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.data.repository.AuthRepositoryImpl
import top.contins.synapse.data.repository.GoalRepositoryImpl
import top.contins.synapse.data.repository.ScheduleRepositoryImpl
import top.contins.synapse.data.repository.TaskRepositoryImpl
import top.contins.synapse.data.repository.TokenRepositoryImpl
import top.contins.synapse.domain.repository.AuthRepository
import top.contins.synapse.domain.repository.GoalRepository
import top.contins.synapse.domain.repository.ScheduleRepository
import top.contins.synapse.domain.repository.TaskRepository
import top.contins.synapse.domain.repository.TokenRepository
import top.contins.synapse.network.api.TokenProvider
import top.contins.synapse.data.storage.TokenManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        tokenManager: TokenManager
    ): TokenProvider

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(
        scheduleRepositoryImpl: ScheduleRepositoryImpl
    ): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        calendarRepositoryImpl: top.contins.synapse.data.repository.CalendarRepositoryImpl
    ): top.contins.synapse.domain.repository.CalendarRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: top.contins.synapse.data.repository.SubscriptionRepositoryImpl
    ): top.contins.synapse.domain.repository.SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindTokenRepository(
        tokenRepositoryImpl: TokenRepositoryImpl
    ): TokenRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: top.contins.synapse.data.repository.chat.ChatRepositoryImpl
    ): top.contins.synapse.domain.repository.chat.ChatRepository
}
