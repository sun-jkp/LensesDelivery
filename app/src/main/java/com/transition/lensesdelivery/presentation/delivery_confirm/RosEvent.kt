package com.transition.lensesdelivery.presentation.delivery_confirm

sealed class RosEvent {
    data object GetHost: RosEvent()
    data object HeartBeats: RosEvent()
}