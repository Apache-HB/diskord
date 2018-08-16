package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.serebit.diskord.JSON
import com.serebit.diskord.network.payloads.Payload
import kotlinx.coroutines.experimental.launch

private val adapter = object : WebSocketAdapter() {
    override fun onTextMessage(websocket: WebSocket, text: String) {
        launch {
            Payload.from(text)?.let { payload ->
                listeners[websocket]?.forEach { it(payload) }
            }
        }
    }
}

private val listeners = mutableMapOf<WebSocket, MutableList<suspend (Payload) -> Unit>>()

internal fun WebSocket.onPayload(callback: suspend (Payload) -> Unit) {
    if (this !in listeners) {
        addListener(adapter)
        listeners[this] = mutableListOf()
    }
    listeners[this]?.add(callback)
}

internal fun WebSocket.clearAdapter() {
    listeners[this]?.clear()
}

internal fun WebSocket.send(obj: Any): WebSocket? = sendText(JSON.stringify(obj))
