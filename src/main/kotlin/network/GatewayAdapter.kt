package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.neovisionaries.ws.client.WebSocketFrame
import com.serebit.diskord.Serializer
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.gateway.DispatchType
import com.serebit.diskord.gateway.GatewayCloseCodes
import com.serebit.diskord.gateway.Opcodes
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.gateway.PostCloseAction
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.runBlocking
import org.json.JSONObject
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class GatewayAdapter(
    private val uri: String,
    private val eventDispatcher: EventDispatcher,
    private inline val onReady: (Payload.Dispatch.Ready) -> Unit
) {
    private val heartbeatManager = ScheduledThreadPoolExecutor(1)
    private val factory = WebSocketFactory()
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var socket: WebSocket? = null

    fun openGateway() {
        socket = factory.createSocket(uri)
            .addListener(object : WebSocketAdapter() {
                override fun onTextMessage(socket: WebSocket, text: String) = handlePayload(socket, text)

                override fun onDisconnected(
                    socket: WebSocket,
                    serverCloseFrame: WebSocketFrame,
                    clientCloseFrame: WebSocketFrame,
                    closedByServer: Boolean
                ) = handleClose(serverCloseFrame.closeCode)
            })
            .connect()
    }

    fun closeGateway() {
        socket?.sendClose(1000, "Normal closure.")
    }

    private fun resumeGateway(sessionId: String) {
        socket = factory.createSocket(uri)
            .addListener(object : WebSocketAdapter() {
                override fun onConnected(socket: WebSocket, headers: Map<String, List<String>>) {
                    val payload = Payload.Resume(Payload.Resume.Data(ApiRequester.token, sessionId, lastSequence))
                    socket.sendText(Serializer.toJson(payload))
                }

                override fun onTextMessage(socket: WebSocket, text: String) = handlePayload(socket, text)

                override fun onDisconnected(
                    socket: WebSocket,
                    serverCloseFrame: WebSocketFrame,
                    clientCloseFrame: WebSocketFrame,
                    closedByServer: Boolean
                ) = handleClose(serverCloseFrame.closeCode)
            })
            .connect()
    }

    private fun initializeGateway(socket: WebSocket, payload: Payload.Hello) {
        heartbeatManager.scheduleAtFixedRate({
            socket.sendText(Serializer.toJson(Payload.Heartbeat(lastSequence)))
        }, 0L, payload.d.heartbeat_interval.toLong(), TimeUnit.MILLISECONDS)

        socket.sendText(Serializer.toJson(Payload.Identify(ApiRequester.identification)))
    }

    private fun handlePayload(socket: WebSocket, text: String) = runBlocking {
        when (JSONObject(text)["op"]) {
            Opcodes.hello -> initializeGateway(socket, Serializer.fromJson(text))
            Opcodes.dispatch -> if (JSONObject(text)["t"] in DispatchType.values().map { it.name }) {
                val dispatch = Serializer.fromJson<Payload.Dispatch>(text)
                if (dispatch is Payload.Dispatch.Ready) onReady(dispatch)
                processEvent(dispatch)
            }
        }
        Logger.trace(text)
    }

    private fun handleClose(code: Int) {
        GatewayCloseCodes.valueOf(code)?.let {
            when (it.action) {
                PostCloseAction.CLOSE -> {
                    Logger.info(it.message)
                    Runtime.getRuntime().halt(0)
                }
                PostCloseAction.RESUME -> sessionId?.let { sessionId ->
                    Logger.warn(it.message)
                    resumeGateway(sessionId)
                }
                PostCloseAction.RESTART -> {
                    Logger.error(it.message)
                    openGateway()
                }
            }
        }
    }

    private suspend fun processEvent(payload: Payload.Dispatch) {
        lastSequence = payload.s
        eventDispatcher.dispatch(payload)
    }
}
