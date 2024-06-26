package com.transition.lensesdelivery.presentation.delivery_confirm

sealed class QueueEvent {
    data object Refresh : QueueEvent()
    data class OnConfirm(val buttonId: Int) : QueueEvent()
}