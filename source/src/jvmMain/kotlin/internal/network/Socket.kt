package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JSON
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import kotlin.coroutines.CoroutineContext

internal actual class Socket actual constructor(private val uri: String) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var listeners = mutableListOf<suspend (Payload) -> Unit>()
    private lateinit var webSocket: Websocket
    actual var isOpen = false
        private set
    actual val isClosed get() = !isOpen

    actual fun connect() {
        webSocket = WebsocketClient.nonBlocking(Uri.of(uri)) {
            isOpen = true
        }
        webSocket.apply {
            onMessage { message ->
                val body = message.bodyString()
                Payload.from(body)?.let { payload ->
                    launch {
                        listeners.asSequence().forEach { it(payload) }
                    }
                }
            }

            onClose {
                isOpen = false
            }
        }
    }

    actual fun send(text: String) {
        webSocket.send(WsMessage(text))
    }

    actual fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(JSON.stringify(serializer, obj))
    }

    actual fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners.add(callback)
    }

    actual fun clearListeners() {
        listeners = mutableListOf()
    }

    actual fun close(code: GatewayCloseCode) {
        webSocket.close(WsStatus(code.code, code.message))
    }
}
