package com.transition.lensesdelivery.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface QueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueues(
        queueEntity: List<QueueEntity>
    )

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertQueue(
//        queueEntity: QueueEntity
//    )

    @Query("DELETE FROM RSQueue")
    suspend fun clearQueue()

    @Query("DELETE FROM RSQueue WHERE QUEUE_ID=:queueId")
    suspend fun clearQueueById(queueId: Int)

    @Query(
        """
            SELECT *
            FROM RSQueue
        """
    )
    suspend fun searchQueue(): List<QueueEntity>

    @Query(
        """
            SELECT *
            FROM RSQueue
            WHERE STATUS_ID>=1 AND STATUS_ID<=5
        """
    )
    suspend fun searchQueueNotSuccess(): List<QueueEntity>

    @Query(
        """
            SELECT *
            FROM RSQueue
            WHERE STATUS_ID>=1 AND STATUS_ID <= 5
            LIMIT 1
        """
    )
    suspend fun searchQueueOne(): QueueEntity?

    @Query(
        """
            SELECT *
            FROM RSQueue
            WHERE QUEUE_ID=:queueId
        """
    )
    suspend fun searchQueueById(queueId: Int): QueueEntity

    @Query(
        """
            SELECT *
            FROM RSQueue
            WHERE STATUS_ID>=6 AND STATUS_ID<=7
        """
    )
    suspend fun searchQueueSuccess(): List<QueueEntity>

    @Update
    suspend fun updateQueue(queueEntity: QueueEntity)

}