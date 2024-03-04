package com.transition.lensesdelivery.di

import com.transition.lensesdelivery.data.repository.QueueRepositoryImpl
import com.transition.lensesdelivery.data.repository.SocketRepositoryImpl
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.repository.QueueRepository
import com.transition.lensesdelivery.domain.repository.SocketRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQueueRepository(
        queueRepositoryImpl: QueueRepositoryImpl
    ): QueueRepository

    @Binds
    @Singleton
    abstract fun bindSocketRepository(
        socketRepositoryImpl: SocketRepositoryImpl
    ): SocketRepository

}