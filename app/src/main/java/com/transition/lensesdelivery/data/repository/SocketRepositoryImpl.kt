package com.transition.lensesdelivery.data.repository

import com.transition.lensesdelivery.domain.repository.SocketRepository
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketRepositoryImpl @Inject constructor(private val socket: Socket) : SocketRepository {

    override fun connect() {
        try {
            socket.connect()

            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("message", "Robot Service join to server.")
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    override fun disconnect() {
        socket.disconnect()
    }

    override fun emitEvent(eventName: String, data: Any?) {
        socket.emit(eventName, data)
    }

    override fun listenForEvent(eventName: String, listener: Emitter.Listener) {
        socket.on(eventName, listener)
    }
}