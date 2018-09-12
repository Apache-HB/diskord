package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.payloads.Payload
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import kotlin.coroutines.experimental.CoroutineContext

internal class Socket(private val uri: String) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    private var webSocket: Websocket? = null
    var isOpen = false
        private set
    val isClosed get() = !isOpen

    fun connect() {
        webSocket = WebsocketClient.nonBlocking(Uri.of(uri)) {
            isOpen = true
        }
        webSocket?.apply {
            onMessage { message ->
                launch {
                    Payload.from(message.bodyString())?.let { payload ->
                        listeners.forEach { it(payload) }
                    }
                }
            }

            onClose {
                isOpen = false
            }
        }
    }

    fun send(text: String) {
        webSocket?.send(WsMessage(text))
    }

    fun send(obj: Any) = send(JSON.stringify(obj))

    fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners += callback
    }

    fun clearListeners() = listeners.clear()

    fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE) {
        webSocket?.close(WsStatus(code.code, code.message))
    }
}
