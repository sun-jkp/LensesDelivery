package com.transition.lensesdelivery.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QueueEntity::class],
    version = 1
)
abstract class QueueDatabase : RoomDatabase() {
    abstract val dao: QueueDao
}