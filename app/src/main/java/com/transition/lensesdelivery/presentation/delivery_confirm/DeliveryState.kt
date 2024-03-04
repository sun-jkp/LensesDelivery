package com.transition.lensesdelivery.presentation.delivery_confirm

import com.transition.lensesdelivery.domain.model.Queue

data class DeliveryState(
    val queue: List<Queue> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val isNavigate: Boolean = false,
    val isPlayMusic: Boolean = false
)
