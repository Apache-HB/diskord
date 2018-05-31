package com.serebit.diskord.network

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import com.serebit.diskord.Serializer
import com.serebit.diskord.events.EventDispatcher
import com.serebit.diskord.gateway.DispatchType
import com.serebit.diskord.gateway.Opcodes
import com.serebit.diskord.gateway.Payload
import kotlinx.coroutines.experimental.launch
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class GatewayAdapter(private val eventDispatcher: EventDispatcher) : WebSocketListener() {
    private val serializer = GsonBuilder().serializeNulls().create()
    private val heartbeatManager = ScheduledThreadPoolExecutor(1)
    private var lastSequence: Int? = null

    private fun initializeGateway(webSocket: WebSocket, payload: Payload.Hello) {
        heartbeatManager.scheduleAtFixedRate({
            val heartbeat = serializer.toJson(Payload.Heartbeat(lastSequence))
            webSocket.send(heartbeat)
        }, 0L, payload.d.heartbeat_interval.toLong(), TimeUnit.MILLISECONDS)

        webSocket.send(
            serializer.toJson(Payload.Identify(ApiRequester.identification))
        )
    }

    private fun processEvent(dispatch: Payload.Dispatch) {
        lastSequence = dispatch.s
        dispatch.asEvent?.let(eventDispatcher::dispatch)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        launch {
            when (JSONObject(text)["op"]) {
                Opcodes.hello -> initializeGateway(webSocket, serializer.fromJson(text))
                Opcodes.dispatch -> if (JSONObject(text)["t"] in DispatchType.values().map { it.name }) {
                    processEvent(Serializer.fromJson(text))
                }
            }
            println(text)
        }
    }
}
