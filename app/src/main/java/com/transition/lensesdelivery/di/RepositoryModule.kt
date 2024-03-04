package com.transition.lensesdelivery.di

import com.transition.lensesdelivery.data.csv.CSVParser
import com.transition.lensesdelivery.data.csv.QueueParser
import com.transition.lensesdelivery.data.repository.QueueRepositoryImpl
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.repository.QueueRepository
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
    abstract fun bindQueueParser(
        queueParser: QueueParser
    ): CSVParser<Queue>

    @Binds
    @Singleton
    abstract fun bindQueueRepository(
        queueRepositoryImpl: QueueRepositoryImpl
    ): QueueRepository
}