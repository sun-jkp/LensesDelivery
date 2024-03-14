package com.transition.lensesdelivery.presentation.delivery_confirm

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.transition.lensesdelivery.R
import kotlinx.coroutines.delay

const val TAG = "TEST"

@Composable
fun DeliveryScreen(
    player: ExoPlayer,
    viewModel: DeliveryViewModel = hiltViewModel()
) {
    val deliveryState by viewModel.deliveryState.collectAsState()
    val context = LocalContext.current
    player.prepare()
    player.repeatMode = Player.REPEAT_MODE_ALL
    LaunchedEffect(Unit) {
        val musicId = R.raw.corporate_tech
        val path = "android.resource://raw/$musicId"
        val mediaItem = MediaItem.fromUri(Uri.parse(path))

        player.addMediaItem(mediaItem)
//        player.play()
        viewModel.initController(context)
        delay(200)
        if(deliveryState.isRosConnected){
            viewModel.onRosEvent(RosEvent.GetSpecialArea)
            viewModel.checkQueueInCache()
        }

        while (true) {
//            if(deliveryState.isRosConnected) viewModel.onRosEvent(RosEvent.HeartBeats)
//            Log.d("TEST","${deliveryState.isNavigate}")
            delay(5000L)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            player.release()
        }
    }

//    Log.d(TAG, "isPlayMusic = ${deliveryState.isPlayMusic}")
    if(deliveryState.isPlayMusic){
        player.play()
    }else{
        player.stop()
    }

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            BatteryInfo(deliveryState.coreData.power, deliveryState.coreData.chargingStatus == 2)
            Spacer(modifier = Modifier.width(10.dp))
            RobotStatus(
                isConnected = deliveryState.isConnected,
                isRosConnected = deliveryState.isRosConnected,
                massage = deliveryState.robotMsg
            )
        }
        QueueDetailLayout(queueDetail = deliveryState.queueDetail)

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

@Preview(showBackground = true)
@Composable
fun Screen() {
//    DeliveryScreen()
}