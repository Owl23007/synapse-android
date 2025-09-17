package top.contins.synapse.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.data.repository.WritingRepository
import top.contins.synapse.data.repository.WritingRepositoryImpl
import top.contins.synapse.data.storage.TokenManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindWritingRepository(
        writingRepositoryImpl: WritingRepositoryImpl
    ): WritingRepository
}