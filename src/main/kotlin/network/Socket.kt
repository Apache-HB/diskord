package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import com.serebit.diskord.JSON
import com.serebit.diskord.network.payloads.Payload
import kotlinx.coroutines.experimental.launch

internal class Socket(uri: String) {
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    private val listenerAdapter = object : WebSocketAdapter() {
        override fun onTextMessage(websocket: WebSocket, text: String) {
            launch {
                Payload.from(text)?.let { payload ->
                    listeners.forEach { it(payload) }
                }
            }
        }
    }
    private val webSocket = factory.createSocket(uri).addListener(listenerAdapter)
    val isOpen get() = webSocket.isOpen
    val isClosed get() = !isOpen

    fun connect() {
        webSocket.connect()
    }

    fun send(text: String) {
        webSocket.sendText(text)
    }

    fun send(obj: Any) = send(JSON.stringify(obj))

    fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners += callback
    }

    fun clearListeners() = listeners.clear()

    fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE) {
        webSocket.sendClose(code.code)
    }

    companion object {
        private val factory = WebSocketFactory()
    }
}
