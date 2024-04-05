package com.transition.lensesdelivery.di

import android.app.Application
import androidx.room.Room
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.remote.QueueApi
import com.transition.lensesdelivery.domain.repository.SocketRepository.Companion.SOCKET_IO_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class )
object AppModule {
    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .readTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideQueueApi(okHttpClient: OkHttpClient): QueueApi {
        return Retrofit.Builder()
            .baseUrl(QueueApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }


    @Provides
    @Singleton
    fun providesQueueDatabase(app: Application): QueueDatabase {
        return Room.databaseBuilder(
            app,
            QueueDatabase::class.java,
            "RSDB.db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesSocket(): Socket {
        return IO.socket(SOCKET_IO_URL)
    }

//    @Provides
//    @Singleton
//    fun provideVideoPlayer(app: Application): Player {
//        return ExoPlayer.Builder(app)
//            .build()
//    }
}