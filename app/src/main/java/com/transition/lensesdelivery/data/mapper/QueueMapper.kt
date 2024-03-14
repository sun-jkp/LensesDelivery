package com.transition.lensesdelivery.data.mapper

import com.transition.lensesdelivery.data.local.LocalDateTimeConverter
import com.transition.lensesdelivery.data.local.QueueEntity
import com.transition.lensesdelivery.data.remote.Dto.QueueDto
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.QueueDetail

private val timeConverter = LocalDateTimeConverter()
fun QueueEntity.toQueue(): Queue {
    return Queue(
        QUEUE_ID = queueId,
        RS_ID = rsId,
        STATUS_ID = statusId,
        PICKUP_POINT_ID = pickupPointId,
        DESTINATION_POINT_ID = destinationPointId,
        PRODUCT_TYPE_ID = productTypeId,
        JOB_TYPE_ID = jobTypeId,
        CALLER_BY = callerBy,
//        CALL_TIME = timeConverter.timeToString(callTime),
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

fun Queue.toQueueEntity(): QueueEntity {
    return QueueEntity(
        queueId = QUEUE_ID,
        rsId = RS_ID,
        statusId = STATUS_ID,
        pickupPointId = PICKUP_POINT_ID,
        destinationPointId = DESTINATION_POINT_ID,
        productTypeId = PRODUCT_TYPE_ID,
        jobTypeId = JOB_TYPE_ID,
        callerBy = CALLER_BY,
//        callTime = timeConverter.stringToTime(CALL_TIME),
        pickupTime = timeConverter.stringToTime(PICKUP_TIME),
        waitPlaceTime = timeConverter.stringToTime(WAIT_PLACE_TIME),
        deliverTime = timeConverter.stringToTime(DELIVER_TIME),
        waitPickTime = timeConverter.stringToTime(WAIT_PICK_TIME),
        checkingTime = timeConverter.stringToTime(CHECKING_TIME),
        finishTime = timeConverter.stringToTime(FINISH_TIME),
        result = RESULT,
        errorCode = ERROR_CODE,
        remark = REMARK
    )
}

fun Queue.toQueueDetail(): QueueDetail {
    return QueueDetail(
        queueId = QUEUE_ID,
        status = statusIdToStr(STATUS_ID),
        pickupPoint = pointIdToStr(PICKUP_POINT_ID),
        destinationPoint = pointIdToStr(DESTINATION_POINT_ID),
        jobType = jobTypeIdToStr(JOB_TYPE_ID),
        productType = productTypeIdToStr(PRODUCT_TYPE_ID)
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
        1 -> {
            "Pending"
        }

        2 -> {
            "Pick Up"
        }

        3 -> {
            "Wait Place Lenses"
        }

        4 -> {
            "Delivery"
        }

        5 -> {
            "Wait Pick Lenses"
        }

        6 -> {
            "Checking"
        }
        7 -> {
            "Success"
        }
        8 -> {
            "Cancel"
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


fun QueueDto.toQueueEntity(): QueueEntity {
    return QueueEntity(
        queueId = QUEUE_ID,
        rsId = RS_ID,
        statusId = STATUS_ID,
        pickupPointId = PICKUP_POINT_ID,
        destinationPointId = DESTINATION_POINT_ID,
        productTypeId = PRODUCT_TYPE_ID,
        jobTypeId = JOB_TYPE_ID,
        callerBy = CALLER_BY,
//        callTime = timeConverter.stringToTime(CALL_TIME),
        pickupTime = timeConverter.stringToTime(PICKUP_TIME),
        waitPlaceTime = timeConverter.stringToTime(WAIT_PLACE_TIME),
        deliverTime = timeConverter.stringToTime(DELIVER_TIME),
        waitPickTime = timeConverter.stringToTime(WAIT_PICK_TIME),
        checkingTime = timeConverter.stringToTime(CHECKING_TIME),
        finishTime = timeConverter.stringToTime(FINISH_TIME),
        result = RESULT,
        errorCode = ERROR_CODE,
        remark = REMARK
    )
}

fun QueueDto.toQueue(): Queue {
    return Queue(
        QUEUE_ID = QUEUE_ID,
        RS_ID = RS_ID,
        STATUS_ID = STATUS_ID,
        PICKUP_POINT_ID = PICKUP_POINT_ID,
        DESTINATION_POINT_ID = DESTINATION_POINT_ID,
        PRODUCT_TYPE_ID = PRODUCT_TYPE_ID,
        JOB_TYPE_ID = JOB_TYPE_ID,
        CALLER_BY = CALLER_BY,
//        CALL_TIME = CALL_TIME,
        PICKUP_TIME = PICKUP_TIME,
        WAIT_PLACE_TIME = WAIT_PLACE_TIME,
        DELIVER_TIME = DELIVER_TIME,
        WAIT_PICK_TIME = WAIT_PICK_TIME,
        CHECKING_TIME = CHECKING_TIME,
        FINISH_TIME = FINISH_TIME,
        RESULT = RESULT,
        ERROR_CODE = ERROR_CODE,
        REMARK = REMARK
    )
}