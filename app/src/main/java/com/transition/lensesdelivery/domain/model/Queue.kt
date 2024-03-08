package com.transition.lensesdelivery.domain.model

data class Queue(
    val QUEUE_ID: Int,
    val RS_ID: Int? = null,
    var STATUS_ID: Int,
    val PICKUP_POINT_ID: Int,
    val DESTINATION_POINT_ID: Int,
    val PRODUCT_TYPE_ID: Int? = null,
    val JOB_TYPE_ID: Int,
    val CALLER_BY: String? = null,
    val CALL_TIME: String? = null,
    var PICKUP_TIME: String? = null,
    var WAIT_PLACE_TIME: String? = null,
    var DELIVER_TIME: String? = null,
    var WAIT_PICK_TIME: String? = null,
    var CHECKING_TIME: String? = null,
    var FINISH_TIME: String? = null,
    val RESULT: Int? = null,
    val ERROR_CODE: String? = null,
    val REMARK: String? = null
)
