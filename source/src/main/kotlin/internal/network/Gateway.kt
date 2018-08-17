package com.serebit.diskord.internal.network

import com.serebit.diskord.Context
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.internal.network.payloads.DispatchPayload
import com.serebit.diskord.internal.network.payloads.HelloPayload
import com.serebit.diskord.internal.network.payloads.IdentifyPayload
import com.serebit.diskord.internal.network.payloads.ResumePayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout

internal class Gateway(uri: String, private val eventDispatcher: EventDispatcher) {
    private val socket = Socket(uri)
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var heart = Heart(socket)

    fun connect(): HelloPayload? = runBlocking {
        var helloPayload: HelloPayload? = null
        socket.onPayload { payload ->
            helloPayload = payload as? HelloPayload
        }
        socket.connect()
        withTimeout(connectionTimeout) {
            while (helloPayload == null) delay(payloadPollDelay)
            socket.clearListeners()
        }
        helloPayload
    }

    fun disconnect() = runBlocking {
        socket.close(GatewayCloseCode.GRACEFUL_CLOSE)
        withTimeout(connectionTimeout) {
            while (socket.isOpen) delay(payloadPollDelay)
        }
        println(GatewayCloseCode.GRACEFUL_CLOSE.message)
    }

    fun openSession(hello: HelloPayload): DispatchPayload.Ready? = runBlocking {
        var readyPayload: DispatchPayload.Ready? = null
        socket.onPayload { payload ->
            if (payload is DispatchPayload) {
                if (payload is DispatchPayload.Ready) {
                    Context.selfUserId = payload.d.user.id
                    readyPayload = payload
                }
                processDispatch(payload)
            }
        }
        sessionId?.let { resumeSession(hello, it) } ?: openNewSession(hello)
        withTimeout(connectionTimeout) {
            while (readyPayload == null) delay(payloadPollDelay)
        }
        readyPayload
    }

    private fun resumeSession(hello: HelloPayload, sessionId: String) {
        startHeartbeat(hello.d.heartbeat_interval)
        socket.send(ResumePayload(Requester.token, sessionId, lastSequence))
    }

    private fun openNewSession(hello: HelloPayload) {
        startHeartbeat(hello.d.heartbeat_interval)
        socket.send(IdentifyPayload(Requester.identification))
    }

    private fun startHeartbeat(interval: Long) = heart.start(interval, ::disconnect)

    private suspend fun processDispatch(dispatch: DispatchPayload) {
        if (dispatch !is DispatchPayload.Unknown) {
            eventDispatcher.dispatch(dispatch)
        } else Logger.trace("Received unknown dispatch with type ${dispatch.t}")
    }

    companion object {
        private const val connectionTimeout = 20000 // ms
        private const val payloadPollDelay = 50 // ms
    }
}