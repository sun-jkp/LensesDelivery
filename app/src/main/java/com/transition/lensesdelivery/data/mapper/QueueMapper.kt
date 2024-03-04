package com.transition.lensesdelivery.data.mapper

import com.transition.lensesdelivery.data.local.QueueEntity
import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import com.transition.lensesdelivery.domain.model.Queue

fun QueueEntity.toQueue(): Queue {
    return Queue(
        QUEUE_ID = queueId,
        STATUS_ID = statusId,
        PICKUP_POINT_ID = pickupPointId,
        DESTINATION_POINT_ID = destinationPointId,
        PRODUCT_TYPE_ID = productTypeId,
        JOB_TYPE_ID = jobTypeId
    )
}

fun Queue.toQueueEntity(): QueueEntity {
    return QueueEntity(
        queueId = QUEUE_ID,
        statusId = STATUS_ID,
        pickupPointId = PICKUP_POINT_ID,
        destinationPointId = DESTINATION_POINT_ID,
        productTypeId = PRODUCT_TYPE_ID,
        jobTypeId = JOB_TYPE_ID
    )
}

fun QueueDto.toQueueEntity(): QueueEntity {
    return QueueEntity(
        queueId = QUEUE_ID,
        statusId = STATUS_ID,
        pickupPointId = PICKUP_POINT_ID,
        destinationPointId = DESTINATION_POINT_ID,
        productTypeId = PRODUCT_TYPE_ID,
        jobTypeId = JOB_TYPE_ID
    )
}