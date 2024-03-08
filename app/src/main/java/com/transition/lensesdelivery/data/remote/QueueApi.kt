package com.transition.lensesdelivery.data.remote

import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.UpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface QueueApi {

    @GET("api/agv/{rsId}")
    suspend fun getQueues(@Path("rsId") rsId: Int): List<QueueDto>

    @GET("api/agv/queue/{queueId}")
    suspend fun getQueueById(@Path("queueId") queueId: Int): QueueDto

    @PUT("api/agv/")
    suspend fun updateQueue(@Body queue: Queue): UpdateResponse

    companion object {
        const val BASE_URL = "http://10.14.39.112:3000"
    }
}