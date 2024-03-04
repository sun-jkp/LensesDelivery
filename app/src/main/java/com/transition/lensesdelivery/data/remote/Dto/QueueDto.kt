package com.transition.lensesdelivery.data.remote.Dto

import java.util.Date

data class QueueDto(
    val QUEUE_ID: Int,
    val RS_ID: Int? = null,
    val STATUS_ID: Int,
    val PICKUP_POINT_ID: Int,
    val DESTINATION_POINT_ID: Int,
    val PRODUCT_TYPE_ID: Int? = null,
    val JOB_TYPE_ID: Int,
    val CALLER_BY: String? = null,
    val CALL_TIME: Date? = null,
    val PICKUP_TIME: Date? = null,
    val WAIT_PLACE_TIME: Date? = null,
    val DELIVER_TIME: Date? = null,
    val WAIT_PICK_TIME: Date? = null,
    val CHECKING_TIME: Date? = null,
    val FINISH_TIME: Date? = null,
    val RESULT: Int? = null,
    val ERROR_CODE: String? = null,
    val REMARK: String? = null
)