package com.transition.lensesdelivery.di

import android.app.Application
import androidx.room.Room
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.remote.QueueApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Provides
    @Singleton
    fun provideQueueApi(): QueueApi{
        return Retrofit.Builder()
            .baseUrl(QueueApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }


    @Provides
    @Singleton
    fun providesQueueDatabase(app: Application): QueueDatabase{
        return Room.databaseBuilder(
            app,
            QueueDatabase::class.java,
            "RSDB.db"
        ).build()
    }
}