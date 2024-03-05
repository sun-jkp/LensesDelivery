package com.transition.lensesdelivery.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.transition.lensesdelivery.data.local.LocalDateTimeConverter
import com.transition.lensesdelivery.data.local.QueueEntity
import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.QueueDetail
import java.time.LocalDateTime

private val timeConverter = LocalDateTimeConverter()
fun QueueEntity.toQueue(): Queue {
    return Queue(
        QUEUE_ID = queueId,
        STATUS_ID = statusId,
        PICKUP_POINT_ID = pickupPointId,
        DESTINATION_POINT_ID = destinationPointId,
        PRODUCT_TYPE_ID = productTypeId,
        JOB_TYPE_ID = jobTypeId,
        CALLER_BY = callerBy,
        CALL_TIME = timeConverter.timeToString(callTime),
        PICKUP_TIME = timeConverter.timeToString(pickupTime),
        WAIT_PLACE_TIME = timeConverter.timeToString(waitPlaceTime),
        DELIVER_TIME = timeConverter.timeToString(deliverTime),
        WAIT_PICK_TIME = timeConverter.timeToString(waitPickTime),
        CHECKING_TIME = timeConverter.timeToString(checkingTime),
        FINISH_TIME = timeConverter.timeToString(finishTime),
        RESULT = result,
        ERROR_CODE = errorCode,
        REMARK = remark
    )
}

fun QueueEntity.toQueueDetail(): QueueDetail {
    return QueueDetail(
        queueId = queueId,
        status = statusIdToStr(statusId),
        pickupPoint = pointIdToStr(pickupPointId),
        destinationPoint = pointIdToStr(destinationPointId),
        jobType = jobTypeIdToStr(jobTypeId),
        productType = productTypeIdToStr(productTypeId)
    )
}

fun jobTypeIdToStr(jobTypeId: Int): String {
    return when (jobTypeId) {
        1 -> {
            "Film Thickness"
        }

        2 -> {
            "Post cure"
        }

        3 -> {
            "Color Change"
        }

        4 -> {
            "Returning lenses"
        }

        else -> {
            "Unknown"
        }

    }
}

fun productTypeIdToStr(productTypeId: Int?): String {
    return when (productTypeId) {
        1 -> {
            "Good"
        }

        2 -> {
            "Reject"
        }

        else -> {
            "Unknown"
        }
    }
}

fun statusIdToStr(statusId: Int): String {
    return when (statusId) {
        0 -> {
            "Lab"
        }

        1 -> {
            "Pick up"
        }

        2 -> {
            "Wait Place"
        }

        3 -> {
            "Delivery"
        }

        4 -> {
            "Wait Pick"
        }

        5 -> {
            "checking"
        }

        6 -> {
            "success"
        }

        else -> {
            "Unknown"
        }
    }
}

fun pointIdToStr(pointId: Int): String {
    return when (pointId) {
        0 -> {
            "Lab"
        }

        1 -> {
            "Line 1"
        }

        2 -> {
            "Line 2"
        }

        3 -> {
            "Line 3"
        }

        4 -> {
            "Line 4"
        }

        5 -> {
            "Line 5"
        }

        6 -> {
            "Line 6"
        }

        7 -> {
            "Line 7"
        }

        8 -> {
            "Line 8"
        }

        else -> {
            "Unknown"
        }
    }
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
        jobTypeId = JOB_TYPE_ID,
        callerBy = CALLER_BY,
        callTime = timeConverter.stringToTime(CALL_TIME),
        pickupTime = timeConverter.stringToTime(PICKUP_TIME),
        waitPlaceTime = timeConverter.stringToTime(WAIT_PLACE_TIME),
        deliverTime = timeConverter.stringToTime(DELIVER_TIME),
        waitPickTime = timeConverter.stringToTime(WAIT_PICK_TIME),
        checkingTime = timeConverter.stringToTime(CHECKING_TIME),
        finishTime = timeConverter.stringToTime(FINISH_TIME)
    )
}