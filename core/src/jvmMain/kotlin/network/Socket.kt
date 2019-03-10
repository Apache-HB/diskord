package com.serebit.strife.internal.network

import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal actual class Socket actual constructor(private val uri: String) {
    private val client = HttpClient { install(WebSockets) }
    private lateinit var outgoingChannel: SendChannel<Frame>
    private lateinit var session: WebSocketSession
    private val supervisor = SupervisorJob()

    actual suspend fun connect(onReceive: suspend (CoroutineScope, String) -> Unit) =
        client.wss(host = uri.removePrefix("wss://")) {
            outgoingChannel = outgoing
            session = this
            val scope = CoroutineScope(coroutineContext + supervisor)
            incoming.mapNotNull { it as? Frame.Text }.consumeEach {
                scope.launch { onReceive(scope, it.readText()) }
            }
        }

    actual suspend fun send(text: String) = outgoingChannel.send(Frame.Text(text))

    actual suspend fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(Json.stringify(serializer, obj))
    }

    actual suspend fun close(code: GatewayCloseCode) {
        session.close(CloseReason(CloseReason.Codes.NORMAL, "Closing normally"))
    }
}
