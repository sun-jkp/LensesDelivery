package com.transition.lensesdelivery.data.repository

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.mapper.toQueue
import com.transition.lensesdelivery.data.mapper.toQueueEntity
import com.transition.lensesdelivery.data.remote.QueueApi
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.UpdateResponse
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
    override suspend fun getQueuesFlow(
        fetchFromRemote: Boolean,
        rsId: Int
    ): Flow<Resource<List<Queue>>> {
        return flow {
            emit(Resource.Loading(true))
//            val localListings = dao.searchQueue()
//            emit(Resource.Success(
//                data = localListings.map { it.toQueue() }
//            ))

//            val isDbEmpty = localListings.isEmpty()
//            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
//            if (shouldJustLoadFromCache) {
//                emit(Resource.Loading(false))
//                return@flow
//            }

            val remoteListings = try {
                val response = api.getQueues(rsId)
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

//            if(remoteListings!=null){
//                if(remoteListings.isNotEmpty()){
//                    for( queue in remoteListings){
//
//                    }
//                }
//            }

            remoteListings?.let { listings ->
//                dao.clearQueue()
                dao.insertQueues(
                    listings.map { it.toQueueEntity() }
                )
                emit(
                    Resource.Success(
                        data = dao
                            .searchQueue()
                            .map { it.toQueue() })
                )
                emit(Resource.Loading(false))
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun updateQueue(queue: Queue): Resource<UpdateResponse> {
        return try {
            val result = api.updateQueue(queue)
            Resource.Success(result)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't update queue"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't update queue"
            )
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getQueueById(queueId: Int): Resource<Queue> {
        return try {
            val result = api.getQueueById(queueId)
            Resource.Success(result.toQueue())
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't get queue"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't get queue"
            )
        }
    }

}