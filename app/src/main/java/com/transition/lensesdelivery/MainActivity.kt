package com.transition.lensesdelivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.transition.lensesdelivery.presentation.delivery_confirm.DeliveryScreen
import com.transition.lensesdelivery.ui.theme.LensesDeliveryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    private val viewModel: DeliveryViewModel by viewModels<DeliveryViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LensesDeliveryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                        DeliveryScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        viewModel.initController(this)
    }

    override fun onPause() {
        super.onPause()
//        viewModel.stopListen()
    }
}
