package com.transition.lensesdelivery.presentation.delivery_confirm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeman.ros.controller.RobotActionController
import com.reeman.ros.listen.RosCallBackListener
import com.transition.lensesdelivery.domain.repository.QueueRepository
import com.transition.lensesdelivery.domain.repository.SocketRepository
import com.transition.lensesdelivery.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val repository: QueueRepository,
    private val socketRepository: SocketRepository,
) : ViewModel(), RosCallBackListener {

    private val _deliveryState = MutableStateFlow(DeliveryState())
    val deliveryState = _deliveryState.asStateFlow()

    private var searchJob: Job? = null

    private val controller: RobotActionController = RobotActionController.getInstance()

    init {
        getQueue()
        listenServer()
    }

    override fun onCleared() {
        super.onCleared()
        socketDisconnect()
        stopListen()
    }

    fun onEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.Refresh -> {
                getQueue(fetchFromRemote = true)
            }

            is QueueEvent.OnSearchQueryChange -> {
                _deliveryState.update {
                    it.copy(searchQuery = event.query)
                }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getQueue()
                }
            }

            is QueueEvent.OnConfirm -> {
//                event.buttonId
            }
        }
    }

    fun onRosEvent(event: RosEvent) {
        when (event) {
            is RosEvent.GetHost ->{
                controller.getHostIp()
                controller.getHostName()
            }

            is RosEvent.HeartBeats -> {
                heartBeats()
            }
        }
    }

    private fun getQueue(
        fetchFromRemote: Boolean = false,
    ) {
        viewModelScope.launch {
            repository
                .getQueue(fetchFromRemote)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                _deliveryState.update { it.copy(queue = listings) }
                            }
                        }
                        is Resource.Error -> Unit
                        is Resource.Loading -> {
                            _deliveryState.update { it.copy(isLoading = result.isLoading) }
                        }
                    }
                }
        }
    }

    override fun onResult(result: String?) {
        if (result != null) {
            Log.i(TAG, "$result")
        }
    }

    //Ros
    private fun heartBeats(){
        controller.heartBeat()
        Log.i(TAG, "HeartBeat")
    }

    fun initController(context: Context) {
        try {
            controller.init(context, "ros_demo", this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopListen() {
        controller.stopListen()
    }

    //Socket IO
    private fun listenServer() {
        viewModelScope.launch {
            socketRepository.connect()
            socketRepository.listenForEvent("message") { args ->
                _deliveryState.update { it.copy(message = "${args[0]}") }
            }
        }
    }

    private fun socketDisconnect() {
        viewModelScope.launch {
            socketRepository.disconnect()
        }
    }
}