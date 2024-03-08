package com.transition.lensesdelivery.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "RSQueue")
data class QueueEntity(
    @ColumnInfo(name = "QUEUE_ID") @PrimaryKey val queueId: Int,
    @ColumnInfo(name = "RS_ID") val rsId: Int? = null,
    @ColumnInfo(name = "STATUS_ID") val statusId: Int,
    @ColumnInfo(name = "PICKUP_POINT_ID") val pickupPointId: Int,
    @ColumnInfo(name = "DESTINATION_POINT_ID") val destinationPointId: Int,
    @ColumnInfo(name = "PRODUCT_TYPE_ID") val productTypeId: Int?,
    @ColumnInfo(name = "JOB_TYPE_ID") val jobTypeId: Int,
    @ColumnInfo(name = "CALLER_BY") val callerBy: String? = null,
    @ColumnInfo(name = "CALL_TIME") val callTime: Date? = null,
    @ColumnInfo(name = "PICKUP_TIME") val pickupTime: Date? = null,
    @ColumnInfo(name = "WAIT_PLACE_TIME") val waitPlaceTime: Date? = null,
    @ColumnInfo(name = "DELIVER_TIME") val deliverTime: Date? = null,
    @ColumnInfo(name = "WAIT_PICK_TIME") val waitPickTime: Date? = null,
    @ColumnInfo(name = "CHECKING_TIME") val checkingTime: Date? = null,
    @ColumnInfo(name = "FINISH_TIME") val finishTime: Date? = null,
    @ColumnInfo(name = "RESULT") val result: Int? = null,
    @ColumnInfo(name = "ERROR_CODE") val errorCode: String? = null,
    @ColumnInfo(name = "REMARK") val remark: String? = null
)

//@ColumnInfo(name = "CALL_TIME")  val callTime: LocalDateTime? = null,
//@ColumnInfo(name = "PICKUP_TIME")  val pickupTime: LocalDateTime? = null,
//@ColumnInfo(name = "WAIT_PLACE_TIME")  val waitPlaceTime: LocalDateTime? = null,
//@ColumnInfo(name = "DELIVER_TIME")  val deliverTime: LocalDateTime? = null,
//@ColumnInfo(name = "WAIT_PICK_TIME")  val waitPickTime: LocalDateTime? = null,
//@ColumnInfo(name = "CHECKING_TIME")  val checkingTime: LocalDateTime? = null,
//@ColumnInfo(name = "FINISH_TIME")  val finishTime: LocalDateTime? = null,
class LocalDateTimeConverter {
    private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.sss'Z'"

    @TypeConverter
    fun timeToString(time: Date?): String? {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return time?.let { dateFormat.format(it) }
    }

    @TypeConverter
    fun stringToTime(string: String?): Date? {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        var parsedDate: Date? = null
        parsedDate = string?.let { dateFormat.parse(it) }
        return parsedDate
    }
}