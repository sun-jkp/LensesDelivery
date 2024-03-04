package com.transition.lensesdelivery.data.repository

import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.mapper.toQueue
import com.transition.lensesdelivery.data.mapper.toQueueEntity
import com.transition.lensesdelivery.data.remote.QueueApi
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.repository.QueueRepository
import com.transition.lensesdelivery.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueRepositoryImpl @Inject constructor(
    private val api: QueueApi,
    private val db: QueueDatabase,
) : QueueRepository {

    private val dao = db.dao

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getQueue(
        fetchFromRemote: Boolean
    ): Flow<Resource<List<Queue>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListings = dao.searchQueue()
//            Log.i("TEST", "$localListings")
            emit(Resource.Success(
                data = localListings.map { it.toQueue() }
            ))

            val isDbEmpty = localListings.isEmpty()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }

            val remoteListings = try {
                val response = api.getQueues()
//                Log.i("TEST", "$response")
               response
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }

            remoteListings?.let { listings ->
//                Log.i("TEST", "Insert: $listings")
                dao.clearQueue()
                dao.insertQueue(
                    listings.map { it.toQueueEntity() }
                )
                emit(
                    Resource.Success(
                        data = dao
                            .searchQueue()
                            .map { it.toQueue() })
//                    Resource.Success(data = listings)
                )
                emit(Resource.Loading(false))
            }
        }
    }

}