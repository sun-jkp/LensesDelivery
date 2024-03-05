package com.transition.lensesdelivery.presentation.delivery_confirm

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

const val TAG = "TEST"

@Composable
fun DeliveryScreen(
    viewModel: DeliveryViewModel = hiltViewModel()
) {
    val deliveryState by viewModel.deliveryState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.initController(context)
        while (true) {
//            viewModel.onEvent(QueueEvent.Refresh)
            delay(5000L)
            viewModel.onRosEvent(RosEvent.HeartBeats)
        }
    }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.End)
        ) {
            BatteryInfo(99, true)
        }
        QueueDetailLayout(queueDetail = null)

        Spacer(modifier = Modifier.height(5.dp))

        ButtonLayout(deliveryState.buttonState) {
            viewModel.onEvent(QueueEvent.OnConfirm(it))
        }
    }
}

@Composable
fun ButtonLayout(buttonEnable: List<Boolean>, onClick: (buttonId: Int) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LineButton(label = "Line 1", isEnable = buttonEnable[1]) {
                onClick(1)
            }
            LineButton(label = "Line 2", isEnable = buttonEnable[2]) {
                onClick(2)
            }
            LineButton(label = "Line 3", isEnable = buttonEnable[3]) {
                onClick(3)
            }
            LineButton(label = "Line 4", isEnable = buttonEnable[4]) {
                onClick(4)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LineButton(label = "Line 5", isEnable = buttonEnable[5]) {
                onClick(5)
            }
            LineButton(label = "Line 6", isEnable = buttonEnable[6]) {
                onClick(6)
            }
            LineButton(label = "Line 7", isEnable = buttonEnable[7]) {
                onClick(7)
            }
            LineButton(label = "Line 8", isEnable = buttonEnable[8]) {
                onClick(8)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            LabButton(isEnable = buttonEnable[0]) {
                onClick(0)
            }
        }
    }
}