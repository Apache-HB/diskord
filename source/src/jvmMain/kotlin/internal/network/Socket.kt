package com.serebit.strife.internal.network

import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.Payload
import com.serebit.strife.internal.dispatches.Ready
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import kotlin.coroutines.CoroutineContext

internal actual class Socket actual constructor(private val uri: String) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private lateinit var webSocket: Websocket
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    private var onHelloPayload: (suspend (HelloPayload) -> Unit)? = null
    private var onReadyDispatch: (suspend (Ready) -> Unit)? = null
    actual var isOpen = false
        private set

    actual fun connect() {
        webSocket = WebsocketClient.nonBlocking(Uri.of(uri)) {
            isOpen = true
        }
        webSocket.apply {
            onMessage { message ->
                val body = message.bodyString()
                Payload.from(body)?.let { payload ->
                    launch {
                        if (payload is HelloPayload && onHelloPayload != null) {
                            onHelloPayload?.invoke(payload)
                            onHelloPayload = null
                        } else if (payload is Ready && onReadyDispatch != null) {
                            onReadyDispatch?.invoke(payload)
                            onReadyDispatch = null
                        }
                        listeners.forEach { it(payload) }
                    }
                }
            }

            onClose {
                isOpen = false
            }
        }
    }

    actual fun send(text: String) = webSocket.send(WsMessage(text))

    actual fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(Json.stringify(serializer, obj))
    }

    actual fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners.add(callback)
    }

    actual fun onHelloPayload(callback: suspend (HelloPayload) -> Unit) {
        onHelloPayload = callback
    }

    actual fun onReadyDispatch(callback: suspend (Ready) -> Unit) {
        onReadyDispatch = callback
    }

    actual fun clearListeners() = listeners.clear()

    actual fun close(code: GatewayCloseCode) = webSocket.close(WsStatus(code.code, code.message))
}
