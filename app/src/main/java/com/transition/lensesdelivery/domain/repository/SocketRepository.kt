package com.transition.lensesdelivery.domain.repository

import io.socket.emitter.Emitter

interface SocketRepository {
    fun connect()
    fun disconnect()
    fun emitEvent(eventName: String, data: Any?)
    fun listenForEvent(eventName: String, listener: Emitter.Listener)

    companion object {
        const val SOCKET_IO_URL = "http://10.14.39.112:3000"
    }
}