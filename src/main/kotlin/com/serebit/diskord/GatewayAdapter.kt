package com.serebit.diskord

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import khttp.post
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
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
            Payload.Identify.Data(
                token, mapOf(
                    "\$os" to "linux",
                    "\$browser" to "diskord",
                    "\$device" to "diskord"
                )
            )
        )

        webSocket.send(serializer.toJson(identifyPayload))
    }

    private fun processEvent(dispatch: JSONObject) {
        val payload = when (dispatch["t"]) {
            "MESSAGE_CREATE" -> serializer.fromJson<Payload.Dispatch.MessageCreate>(dispatch.toString())
            else -> null
        }

        payload?.let {
            println(it.d.content)
            val content = "my name jeff"
            val response = post(
                "https://discordapp.com/api/v6/channels/${it.d.channel_id}/messages",
                headers = mapOf(
                    "User-Agent" to "DiscordBot (https://github.com/serebit/diskord, $version)",
                    "Authorization" to "Bot $token",
                    "Content-Type" to "application/json"
                ),
                data = mapOf(
                    "content" to content
                )
            )
            println(response.text)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        when (JSONObject(text)["op"]) {
            Opcodes.hello -> initializeGateway(webSocket, serializer.fromJson(text))
            Opcodes.dispatch -> processEvent(JSONObject(text))
        }
        println(text)
    }
}
