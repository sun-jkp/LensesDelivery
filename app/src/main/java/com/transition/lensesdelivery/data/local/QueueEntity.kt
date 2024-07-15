package com.transition.lensesdelivery.data.local

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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


class LocalDateTimeConverter {
//    private val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private val dateFormats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    private val regexPatterns = arrayOf(
        Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z\$"),
        Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z\$")
    )

    @TypeConverter
    fun timeToString(time: Date?): String? {
        Log.d("TEST", "TTS: Date is $time")
        val dateFormat = SimpleDateFormat(dateFormats[0], Locale.getDefault())
//        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Bangkok")
        val timeResult :String? = time?.let { dateFormat.format(it) }
        Log.d("TEST", "TTS String is $timeResult")
        return timeResult
    }

    @TypeConverter
    fun stringToTime(string: String?): Date? {

        var parsedDate: Date? = null
        Log.d("TEST", "STT: Date string is $string")
        if(string==null){
            return null
        }

        for((cnt, pattern) in regexPatterns.withIndex()){
            if(pattern.matches(string)){
                Log.d("TEST", "Trying ${dateFormats[cnt]} format.")
                val dateFormat = SimpleDateFormat(dateFormats[cnt], Locale.getDefault())
                Log.d("TEST", "STT: Date dateFormat is $dateFormat")
                parsedDate = dateFormat.parse(string)
                Log.d("TEST", "STT: Date is $parsedDate")
                return parsedDate
            }
        }
        return null
    }
}