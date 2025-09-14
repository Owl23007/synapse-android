package top.contins.synapse.domain.di


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import top.contins.synapse.domain.repository.AuthRepository
import top.contins.synapse.domain.repository.impl.AuthRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}