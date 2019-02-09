package com.serebit.strife.internal.network

import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.Payload
import com.serebit.strife.internal.dispatches.Ready
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import java.util.concurrent.Executors

internal actual class Socket actual constructor(private val uri: String) : CoroutineScope {
    override val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private lateinit var webSocket: Websocket
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    actual var onHelloPayload: (suspend (HelloPayload) -> Unit)? = null
    actual var onReadyDispatch: (suspend (Ready) -> Unit)? = null

    actual fun connect() {
        webSocket = WebsocketClient.nonBlocking(Uri.of(uri))
        webSocket.onMessage { message ->
            Payload.from(message.bodyString()).let { payload ->
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
    }

    actual fun send(text: String) = webSocket.send(WsMessage(text))

    actual fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(Json.stringify(serializer, obj))
    }

    actual fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners += callback
    }

    actual fun close(code: GatewayCloseCode, callback: () -> Unit) = webSocket.run {
        close(WsStatus(code.code, code.message))
        callback()
    }
}
