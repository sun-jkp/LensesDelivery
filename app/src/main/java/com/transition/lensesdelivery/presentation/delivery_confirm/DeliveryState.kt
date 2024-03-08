package com.transition.lensesdelivery.presentation.delivery_confirm

import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.QueueDetail

data class DeliveryState(
    val queue: Queue? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val isNavigate: Boolean = false,
    val isPlayMusic: Boolean = false,
    val isConnected: Boolean = false,
    val isRosConnected: Boolean = false,
    val message: String = "",
    val robotMsg: String = "Initializing",
    val queueDetail: QueueDetail? = null,
    val buttonState: MutableList<Boolean> = mutableListOf(
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        false
    ),
    val navResult: NavResult = NavResult(),
    val coreData: CoreData = CoreData(),
    val specialArea: SpecialArea = SpecialArea(),
    val navListPoint: NavListState = NavListState()
)
