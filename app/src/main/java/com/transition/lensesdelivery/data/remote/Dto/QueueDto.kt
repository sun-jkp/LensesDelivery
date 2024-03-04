package com.transition.lensesdelivery.data.remote.Dto

import java.time.LocalDateTime

data class QueueDto(
    val QUEUE_ID: Int,
    val RS_ID: Int? = null,
    val STATUS_ID: Int,
    val PICKUP_POINT_ID: Int,
    val DESTINATION_POINT_ID: Int,
    val PRODUCT_TYPE_ID: Int? = null,
    val JOB_TYPE_ID: Int,
    val CALLER_BY: String? = null,
    val CALL_TIME: String? = null,
    val PICKUP_TIME: String? = null,
    val WAIT_PLACE_TIME: String? = null,
    val DELIVER_TIME: String? = null,
    val WAIT_PICK_TIME: String? = null,
    val CHECKING_TIME: String? = null,
    val FINISH_TIME: String? = null,
    val RESULT: Int? = null,
    val ERROR_CODE: String? = null,
    val REMARK: String? = null
)