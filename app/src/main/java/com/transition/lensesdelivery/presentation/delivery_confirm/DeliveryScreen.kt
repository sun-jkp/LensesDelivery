package com.transition.lensesdelivery.presentation.delivery_confirm

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

const val TAG = "TEST"
@Composable
fun DeliveryScreen(
    viewModel: DeliveryViewModel = hiltViewModel()
) {
    val deliveryState by viewModel.deliveryState.collectAsState()

    LaunchedEffect(Unit) {
        while(true){
            viewModel.onEvent(QueueEvent.Refresh)
            delay(1000L)
            Log.i(TAG, "${deliveryState.queue}")
        }
    }
    Column {
        Text(
            text="Hello World"
        )
        Text(
            text = deliveryState.message
        )
    }


}