package com.transition.lensesdelivery.data.remote

import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.UpdateResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QueueApi {

    @GET("api/agv/{rsId}")
    suspend fun getQueues(@Path("rsId") rsId: Int): List<QueueDto>

    @GET("api/agv/{rsId}/point/{pointId}")
    suspend fun getQueuesByPointId(@Path("rsId") rsId: Int, @Path("rsId") pointId: Int): List<QueueDto>

    @GET("api/agv/{rsId}/job/{jobId}")
    suspend fun getQueuesByJobId(@Path("rsId") rsId: Int, @Path("rsId") jobId: Int): List<QueueDto>

    @GET("api/agv/queue/{queueId}")
    suspend fun getQueueById(@Path("queueId") queueId: Int): QueueDto

    @GET("api/agv/{rsId}/compatible")
    suspend fun getCompatibleQueues(@Path("rsId") rsId: Int, @Query("qip") queueId: Int): List<QueueDto>

    @PUT("api/agv/")
    suspend fun updateQueue(@Body queue: Queue): UpdateResponse

    companion object {
//        const val BASE_URL = "http://10.14.39.112:3000"
        const val BASE_URL = "http://10.14.35.194:3000"
    }
}