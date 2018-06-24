package com.serebit.diskord.network

import com.serebit.diskord.Serializer
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.gateway.DispatchType
import com.serebit.diskord.gateway.GatewayCloseCodes
import com.serebit.diskord.gateway.Opcodes
import com.serebit.diskord.gateway.Payload
import com.serebit.diskord.gateway.PostCloseAction
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

internal class GatewayAdapter(
    uri: String,
    private val eventDispatcher: EventDispatcher,
    private inline val onReady: (Payload.Dispatch.Ready) -> Unit
) {
    private val heartbeatManager = ScheduledThreadPoolExecutor(1)
    private val client = OkHttpClient()
    private val request = Request.Builder().url(uri).build()
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var socket: WebSocket? = null
    private val shutdownHook = thread(false) {
        closeSocket(false)
        // give it a second, the socket closure needs to receive confirmation from Discord. nothing is happening on
        // the main thread, so we sleep it for a bit.
        Thread.sleep(1000L)
    }

    init {
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    fun openSocket(resume: Boolean) {
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(socket: WebSocket, response: Response) {
                if (resume) sessionId?.let { sessionId ->
                    val payload = Payload.Resume(Payload.Resume.Data(ApiRequester.token, sessionId, lastSequence))
                    socket.send(Serializer.toJson(payload))
                }
            }

            override fun onMessage(socket: WebSocket, text: String) = handlePayload(socket, text)

            override fun onClosed(socket: WebSocket, code: Int, reason: String) =
                handleClose(code)
        })
    }

    private fun closeSocket(restart: Boolean) {
        socket?.close(1000, "Normal closure.")
        if (restart) openSocket(true)
    }

    private fun initializeGateway(socket: WebSocket, payload: Payload.Hello) {
        heartbeatManager.scheduleAtFixedRate({
            val heartbeat = Serializer.toJson(Payload.Heartbeat(lastSequence))
            socket.send(heartbeat)
        }, 0L, payload.d.heartbeat_interval.toLong(), TimeUnit.MILLISECONDS)

        socket.send(Serializer.toJson(Payload.Identify(ApiRequester.identification)))
    }

    private fun handlePayload(socket: WebSocket, text: String) {
        launch {
            when (JSONObject(text)["op"]) {
                Opcodes.hello -> initializeGateway(socket, Serializer.fromJson(text))
                Opcodes.dispatch -> if (JSONObject(text)["t"] in DispatchType.values().map { it.name }) {
                    val dispatch = Serializer.fromJson<Payload.Dispatch>(text)
                    if (dispatch is Payload.Dispatch.Ready) onReady(dispatch)
                    processEvent(dispatch)
                }
            }
            println(text)
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
                    openSocket(true)
                }
                PostCloseAction.RESTART -> {
                    Logger.error(it.message)
                    openSocket(false)
                }
            }
        }
    }

    private suspend fun processEvent(payload: Payload.Dispatch) {
        lastSequence = payload.s
        eventDispatcher.dispatch(payload)
    }
}
