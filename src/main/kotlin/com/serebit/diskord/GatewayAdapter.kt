package com.serebit.diskord

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class GatewayAdapter(private val token: String) : WebSocketListener() {
    private val serializer = GsonBuilder().serializeNulls().create()
    private val heartbeatManager = ScheduledThreadPoolExecutor(1)
    private var lastSequence: Int? = null

    private fun initializeGateway(webSocket: WebSocket, payload: Payload.Hello) {
        heartbeatManager.scheduleAtFixedRate({
            val heartbeat = serializer.toJson(Payload.Heartbeat(lastSequence))
            webSocket.send(heartbeat)
        }, 0L, payload.d.heartbeat_interval.toLong(), TimeUnit.MILLISECONDS)

        val identifyPayload = Payload.Identify(
            Payload.Identify.IdentifyData(
                token, mapOf(
                    "\$os" to "linux",
                    "\$browser" to "diskord",
                    "\$device" to "diskord"
                )
            )
        )

        webSocket.send(serializer.toJson(identifyPayload))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (Gson().fromJson<BasicPayload>(text).op == 10) {
            initializeGateway(webSocket, serializer.fromJson(text))
        }
        println(text)
    }

    private data class BasicPayload(val op: Int)
}
