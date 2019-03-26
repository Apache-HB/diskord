package com.serebit.strife.internal.network

import com.serebit.strife.internal.onProcessExit
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal actual class Socket actual constructor(private val uri: String) {
    @UseExperimental(KtorExperimentalAPI::class)
    private val client = HttpClient { install(WebSockets) }
    private var session: WebSocketSession? = null

    @UseExperimental(ObsoleteCoroutinesApi::class)
    actual suspend fun connect(onReceive: suspend (CoroutineScope, String) -> Unit) {
        check(session == null) { "Connect method called on active socket" }

        client.wss(host = uri.removePrefix("wss://")) {
            session = this

            onProcessExit {
                close(GatewayCloseCode.GRACEFUL_CLOSE)
            }

            val supervisorScope = CoroutineScope(coroutineContext + SupervisorJob())
            incoming.mapNotNull { it as? Frame.Text }.consumeEach {
                supervisorScope.launch { onReceive(supervisorScope, it.readText()) }
            }
        }
    }

    actual suspend fun send(text: String) = session.let {
        checkNotNull(it) { "Send method called on inactive socket" }
        it.send(Frame.Text(text))
    }

    actual suspend fun <T : Any> send(serializer: KSerializer<T>, obj: T) {
        send(Json.stringify(serializer, obj))
    }

    actual suspend fun close(code: GatewayCloseCode) = session.let {
        checkNotNull(it) { "Close method called on inactive socket" }
        it.close(CloseReason(code.code, code.message))
    }
}
