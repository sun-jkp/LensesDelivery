package com.transition.lensesdelivery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(
        queueEntity: List<QueueEntity>
    )

    @Query("DELETE FROM RSQueue")
    suspend fun clearQueue()

    @Query(
        """
            SELECT *
            FROM RSQueue
        """
    )
    suspend fun searchQueue(): List<QueueEntity>
}