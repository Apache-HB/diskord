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
import java.util.*
import kotlin.concurrent.fixedRateTimer

internal class GatewayAdapter(
    private val uri: String,
    private val eventDispatcher: EventDispatcher,
    private inline val onReady: (DispatchPayload.Ready) -> Unit
) {
    private var heartbeatTimer: Timer? = null
    private val factory = WebSocketFactory()
    private var lastSequence: Int = 0
    private val socketAdapter = object : WebSocketAdapter() {
        override fun onTextMessage(socket: WebSocket, text: String) = handlePayload(socket, text)

        override fun onDisconnected(
            socket: WebSocket,
            serverCloseFrame: WebSocketFrame, clientCloseFrame: WebSocketFrame,
            closedByServer: Boolean
        ) = handleClose(serverCloseFrame.closeCode, closedByServer)
    }
    private val connectionResumer = object : WebSocketAdapter() {
        override fun onConnected(socket: WebSocket, headers: Map<String, List<String>>) {
            socket.send(ResumePayload(ResumePayload.Data(Requester.token, sessionId, lastSequence)))
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
        heartbeatTimer = fixedRateTimer(period = payload.d.heartbeat_interval) {
            val heartbeat = HeartbeatPayload(lastSequence)
            socket.send(heartbeat)
            Logger.trace("Sent heartbeat payload.")
        }

        socket.send(IdentifyPayload(Requester.identification))
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

    private fun handleClose(code: Int, closedByServer: Boolean) {
        heartbeatTimer?.cancel()
        if (closedByServer) {
            val closeCode = GatewayCloseCodes.valueOf(code)
            when (closeCode?.action) {
                PostCloseAction.CLOSE -> {
                    Logger.info(closeCode.message)
                    Runtime.getRuntime().halt(0)
                }
                PostCloseAction.RESUME -> {
                    Logger.warn(closeCode.message)
                    resumeGateway()
                }
                PostCloseAction.RESTART -> {
                    Logger.error(closeCode.message)
                    openGateway()
                }
            }
        } else {
            Logger.info("Connection closed by client.")
            Runtime.getRuntime().halt(0)
        }
    }

    private suspend fun processDispatch(dispatch: DispatchPayload) {
        lastSequence = dispatch.s
        if (dispatch !is DispatchPayload.Unknown) {
            if (dispatch is DispatchPayload.Ready) onReady(dispatch)
            eventDispatcher.dispatch(dispatch)
        } else Logger.trace("Received unknown dispatch with type ${dispatch.t}")
    }

    private fun WebSocket.send(obj: Any): WebSocket? = sendText(Serializer.toJson(obj))
}
