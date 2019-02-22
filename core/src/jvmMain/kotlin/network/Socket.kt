package com.serebit.strife.internal.network

import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.Payload
import com.serebit.strife.internal.dispatches.Ready
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.filterNotNull
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

internal actual class Socket actual constructor(private val uri: String) : CoroutineScope {
    override val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val client = HttpClient { install(WebSockets) }
    private val listeners = mutableListOf<suspend (Payload) -> Unit>()
    actual var onHelloPayload: (suspend (HelloPayload) -> Unit)? = null
    actual var onReadyDispatch: (suspend (Ready) -> Unit)? = null
    private lateinit var outgoingChannel: SendChannel<Frame>
    private lateinit var job: Job

    actual fun connect() {
        job = launch {
            client.wss(host = uri.removePrefix("wss://")) {
                outgoingChannel = this.outgoing

                for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
                    Payload.from(message.readText()).let { payload ->
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
        }
    }

    actual fun send(text: String) = outgoingChannel.sendBlocking(Frame.Text(text))

    actual fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(Json.stringify(serializer, obj))
    }

    actual fun onPayload(callback: suspend (Payload) -> Unit) {
        listeners += callback
    }

    actual fun close(code: GatewayCloseCode, callback: () -> Unit) = runBlocking {
        job.cancelAndJoin()
        callback()
    }
}
