package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.serebit.diskord.Context
import com.serebit.diskord.JSON
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.network.payloads.DispatchPayload
import com.serebit.diskord.network.payloads.HeartbeatPayload
import com.serebit.diskord.network.payloads.HelloPayload
import com.serebit.diskord.network.payloads.IdentifyPayload
import com.serebit.diskord.network.payloads.Payload
import com.serebit.diskord.network.payloads.ResumePayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import java.util.*
import kotlin.concurrent.fixedRateTimer

internal class Gateway(uri: String, private val eventDispatcher: EventDispatcher) {
    private val socket = factory.createSocket(uri)
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var heartbeat: Timer? = null
    private var receivedAcknowledgement: Boolean = false

    fun connect(): HelloPayload? = runBlocking {
        var helloPayload: HelloPayload? = null
        socket.onMessage { _, text ->
            val payload = Payload.from(text)
            if (payload is HelloPayload) {
                helloPayload = payload
            } else {
                Logger.error("Received unexpected payload $payload")
            }
        }
        withTimeout(10000) {
            socket.connect()
            while (helloPayload == null) delay(50)
        }
        socket.clearListeners()
        helloPayload
    }

    fun disconnect() = runBlocking {
        withTimeout(5000) {
            socket.sendClose(GatewayCloseCodes.GRACEFUL_CLOSE.code)
            while (socket.isOpen) delay(50)
        }
        println(GatewayCloseCodes.GRACEFUL_CLOSE.message)
    }

    fun openSession(hello: HelloPayload): DispatchPayload.Ready? = runBlocking {
        var readyPayload: DispatchPayload.Ready? = null
        socket.onMessage { _, text ->
            launch {
                val payload = Payload.from(text)
                if (payload is DispatchPayload) {
                    if (payload is DispatchPayload.Ready) {
                        Context.selfUserId = payload.d.user.id
                        readyPayload = payload
                    }
                    processDispatch(payload)
                }
            }
        }
        withTimeout(20000) {
            sessionId?.let { resumeSession(hello, it) } ?: openNewSession(hello)
            while (readyPayload == null) delay(50)
        }
        delay(1000)
        readyPayload
    }

    private fun resumeSession(hello: HelloPayload, sessionId: String) {
        startHeartbeat(hello.d.heartbeat_interval)
        socket.send(ResumePayload(ResumePayload.Data(Requester.token, sessionId, lastSequence)))
    }

    private fun openNewSession(hello: HelloPayload) {
        startHeartbeat(hello.d.heartbeat_interval)
        identify()
    }

    private fun startHeartbeat(interval: Long) {
        heartbeat = fixedRateTimer(period = interval) {
            socket.send(HeartbeatPayload(lastSequence))
            Logger.trace("Sent heartbeat payload.")
        }
    }

    private suspend fun processDispatch(dispatch: DispatchPayload) {
        lastSequence = dispatch.s
        if (dispatch !is DispatchPayload.Unknown) {
            eventDispatcher.dispatch(dispatch)
        } else Logger.trace("Received unknown dispatch with type ${dispatch.t}")
    }

    private fun identify() = socket.send(IdentifyPayload(Requester.identification))

    companion object {
        private val factory = WebSocketFactory()

        private inline fun WebSocket.onMessage(crossinline callback: suspend (WebSocket, String) -> Unit) =
            addListener(object : WebSocketAdapter() {
                override fun onTextMessage(websocket: WebSocket, text: String) {
                    launch {
                        callback(websocket, text)
                    }
                }
            })

        private inline fun WebSocket.onClose(crossinline callback: suspend (WebSocketFrame?, Boolean) -> Unit) =
            addListener(object : WebSocketAdapter() {
                override fun onDisconnected(
                    websocket: WebSocket,
                    serverCloseFrame: WebSocketFrame?,
                    clientCloseFrame: WebSocketFrame?,
                    closedByServer: Boolean
                ) = runBlocking { callback(serverCloseFrame, closedByServer) }
            })

        private fun WebSocket.send(obj: Any): WebSocket? = sendText(JSON.stringify(obj))
    }
}
