package com.transition.lensesdelivery.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "RSQueue")
data class QueueEntity(
    @ColumnInfo(name = "QUEUE_ID") @PrimaryKey val queueId: Int,
    @ColumnInfo(name = "RS_ID") val rsId: Int? = null,
    @ColumnInfo(name = "STATUS_ID") val statusId: Int,
    @ColumnInfo(name = "PICKUP_POINT_ID") val pickupPointId: Int,
    @ColumnInfo(name = "DESTINATION_POINT_ID") val destinationPointId: Int,
    @ColumnInfo(name = "PRODUCT_TYPE_ID") val productTypeId: Int?,
    @ColumnInfo(name = "JOB_TYPE_ID") val jobTypeId: Int,
//    @ColumnInfo(name = "CALLER_BY") val callerBy: String? = null,
//    @ColumnInfo(name = "CALL_TIME")  val callTime: Date?,
//    @ColumnInfo(name = "PICKUP_TIME")  val pickupTime: String? = null,
//    @ColumnInfo(name = "WAIT_PLACE_TIME")  val waitPlaceTime: String? = null,
//    @ColumnInfo(name = "DELIVER_TIME")  val deliverTime: String? = null,
//    @ColumnInfo(name = "WAIT_PICK_TIME")  val waitPickTime: String? = null,
//    @ColumnInfo(name = "CHECKING_TIME")  val checkingTime: String? = null,
//    @ColumnInfo(name = "FINISH_TIME")  val finishTime: String? = null,
    @ColumnInfo(name = "RESULT") val result: Int? = null,
    @ColumnInfo(name = "ERROR_CODE") val errorCode: String? = null,
    @ColumnInfo(name = "REMARK") val remark: String? = null
)