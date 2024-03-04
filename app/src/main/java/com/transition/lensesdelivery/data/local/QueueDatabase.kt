package com.transition.lensesdelivery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [QueueEntity::class],
    version = 1
)

@TypeConverters(LocalDateTimeConverter::class)
abstract class QueueDatabase : RoomDatabase() {
    abstract val dao: QueueDao
}