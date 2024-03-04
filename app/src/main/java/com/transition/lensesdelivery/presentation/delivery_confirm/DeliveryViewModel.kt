package com.transition.lensesdelivery.presentation.delivery_confirm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transition.lensesdelivery.domain.repository.QueueRepository
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
    private val repository: QueueRepository
) : ViewModel() {

    private val _deliveryState = MutableStateFlow(DeliveryState())
    val deliveryState = _deliveryState.asStateFlow()

    private var searchJob: Job? = null

    init {
        getQueue()
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

            else -> {}
        }
    }

    private fun getQueue(
        fetchFromRemote: Boolean = false
    ) {
        viewModelScope.launch {
            repository
                .getQueue(fetchFromRemote)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
//                            Log.i("TEST", result.data.)
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
}