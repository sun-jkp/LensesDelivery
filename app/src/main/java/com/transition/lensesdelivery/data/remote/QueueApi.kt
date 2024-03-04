package com.transition.lensesdelivery.data.remote

import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface QueueApi {

    @GET("api/queues")
    suspend fun getQueues(): List<QueueDto>

    companion object{
        const val BASE_URL = "http://10.14.39.112:3000"
    }
}