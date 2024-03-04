package com.transition.lensesdelivery.data.csv

import android.util.Log
import com.opencsv.CSVReader
import com.transition.lensesdelivery.domain.model.Queue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueueParser @Inject constructor() : CSVParser<Queue> {
    override suspend fun parse(stream: InputStream): List<Queue> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .mapNotNull { line ->
                    val queueId = line.getOrNull(0)
                    val statusId = line.getOrNull(2)
                    val pickupPointId = line.getOrNull(3)
                    val destinationPointId = line.getOrNull(4)
                    val productTypeId = line.getOrNull(5)
                    val jobTypeId = line.getOrNull(6)
                    Log.d("TEST", "Queue ID:")
                    Queue(
                        QUEUE_ID = queueId?.toInt() ?: return@mapNotNull null,
                        STATUS_ID = statusId?.toInt() ?: return@mapNotNull null,
                        PICKUP_POINT_ID = pickupPointId?.toInt() ?: return@mapNotNull null,
                        DESTINATION_POINT_ID = destinationPointId?.toInt() ?: return@mapNotNull null,
                        PRODUCT_TYPE_ID = productTypeId?.toInt() ?: return@mapNotNull null,
                        JOB_TYPE_ID = jobTypeId?.toInt() ?: return@mapNotNull null
                    )
                }
                .also {
                    csvReader.close()
                }
        }
    }
}