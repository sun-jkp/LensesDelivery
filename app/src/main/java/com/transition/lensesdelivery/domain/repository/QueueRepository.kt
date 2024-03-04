package com.transition.lensesdelivery.domain.repository

import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.util.Resource
import kotlinx.coroutines.flow.Flow

interface QueueRepository {

    suspend fun getQueue(
        fetchFromRemote: Boolean,
    ): Flow<Resource<List<Queue>>>
}