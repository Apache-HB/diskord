package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.JSON
import com.serebit.diskord.internal.payloads.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import kotlin.coroutines.CoroutineContext

internal actual class Socket actual constructor(private val uri: String) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    private var webSocket: Websocket? = null
    actual var isOpen = false
        private set
    actual val isClosed get() = !isOpen

    actual fun connect() {
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

    actual fun send(text: String) {
        webSocket?.send(WsMessage(text))
    }

    actual fun send(obj: Any) = send(JSON.stringify(obj))

    actual fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners += callback
    }

    actual fun clearListeners() = listeners.clear()

    actual fun close(code: GatewayCloseCode) {
        webSocket?.close(WsStatus(code.code, code.message))
    }
}
