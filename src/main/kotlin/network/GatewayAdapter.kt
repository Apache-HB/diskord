package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.serebit.diskord.Serializer
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.network.payloads.*
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class GatewayAdapter(
    private val uri: String,
    private val eventDispatcher: EventDispatcher,
    private inline val onReady: (DispatchPayload.Ready) -> Unit
) {
    private val heartbeatManager = ScheduledThreadPoolExecutor(1)
    private val factory = WebSocketFactory()
    private var lastSequence: Int = 0
    private val socketAdapter = object : WebSocketAdapter() {
        override fun onTextMessage(socket: WebSocket, text: String) = handlePayload(socket, text)

        override fun onDisconnected(
            socket: WebSocket,
            serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame,
            closedByServer: Boolean
        ) = handleClose(serverCloseFrame.closeCode)
    }
    private val connectionResumer = object : WebSocketAdapter() {
        override fun onConnected(socket: WebSocket, headers: Map<String, List<String>>) {
            socket.send(ResumePayload(ResumePayload.Data(ApiRequester.token, sessionId, lastSequence)))
        }
    }
    private lateinit var sessionId: String
    private var socket: WebSocket? = null

    fun openGateway() {
        socket = factory.createSocket(uri)
            .addListener(socketAdapter)
            .connect()
    }

    fun closeGateway() {
        socket?.sendClose(GatewayCloseCodes.GRACEFUL_CLOSE.code)
    }

    private fun resumeGateway() {
        socket = factory.createSocket(uri)
            .addListener(socketAdapter)
            .addListener(connectionResumer)
            .connect()
    }

    private fun initializeGateway(socket: WebSocket, payload: HelloPayload) {
        heartbeatManager.scheduleAtFixedRate({
            val heartbeat = HeartbeatPayload(lastSequence)
            socket.send(heartbeat)
            Logger.trace("Sent heartbeat payload.")
        }, 0L, payload.d.heartbeat_interval, TimeUnit.MILLISECONDS)

        socket.send(IdentifyPayload(ApiRequester.identification))
    }

    private fun handlePayload(socket: WebSocket, text: String) {
        launch {
            val payload = Payload.from(text)
            when (payload) {
                is HelloPayload -> initializeGateway(socket, payload)
                is DispatchPayload -> processDispatch(payload)
            }
        }
    }

    private fun handleClose(code: Int) {
        GatewayCloseCodes.valueOf(code)?.let {
            when (it.action) {
                PostCloseAction.CLOSE -> {
                    Logger.info(it.message)
                    Runtime.getRuntime().halt(0)
                }
                PostCloseAction.RESUME -> {
                    Logger.warn(it.message)
                    resumeGateway()
                }
                PostCloseAction.RESTART -> {
                    Logger.error(it.message)
                    openGateway()
                }
            }
        }
    }

    private suspend fun processDispatch(dispatch: DispatchPayload) {
        lastSequence = dispatch.s
        if (dispatch !is DispatchPayload.Unknown) {
            if (dispatch is DispatchPayload.Ready) onReady(dispatch)
            eventDispatcher.dispatch(dispatch)
        } else Logger.trace("Received unknown dispatch type with type ${dispatch.t}")
    }

    private fun WebSocket.send(obj: Any): WebSocket? {
        val text = Serializer.toJson(obj)
        return sendText(text)
    }
}
