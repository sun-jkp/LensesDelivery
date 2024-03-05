package com.transition.lensesdelivery.domain.model

data class QueueDetail(
    val queueId: Int,
    val status: String,
    val pickupPoint: String,
    val destinationPoint: String,
    val productType: String,
    val jobType: String,
)
