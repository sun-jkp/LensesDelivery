package com.transition.lensesdelivery.domain.repository

import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.UpdateResponse
import com.transition.lensesdelivery.util.Resource
import kotlinx.coroutines.flow.Flow

interface QueueRepository {

    suspend fun getQueuesFlow(
        fetchFromRemote: Boolean,
        rsId: Int
    ): Flow<Resource<List<Queue>>>

    suspend fun updateQueue(
        queue: Queue
    ): Resource<UpdateResponse>

    suspend fun getQueueById(
        queueId: Int
    ): Resource<Queue>

    suspend fun getQueuesByPointId(
        rsId: Int,
        pointId: Int
    ): Flow<Resource<List<Queue>>>

    suspend fun getQueuesByJobId(
        rsId: Int,
        jobId: Int
    ): Flow<Resource<List<Queue>>>

    suspend fun getCompatibleQueues(
        rsId: Int,
        queueId: Int
    ): Flow<Resource<List<Queue>>>
}