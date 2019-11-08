package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.HeartbeatPayload
import com.serebit.strife.internal.Payload
import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

/**
 * Provides an interface to connect to Discord gateways. This class handles ratelimiting and gives a
 * special exception to [Heartbeats][HeartbeatPayload] to pass without being ratelimited. Once connected, it
 * will take care of the connection until it is closed, hiding all its internals from [Gateway].
 */
@UseExperimental(ExperimentalCoroutinesApi::class, FlowPreview::class)
internal class GatewaySocket(private val logger: Logger) {
    /** A default [HttpClient] with [WebSockets] feature installed. */
    @UseExperimental(KtorExperimentalAPI::class)
    private val client = HttpClient { install(WebSockets) }

    /** A [Channel] used for ratelimiting [Frames][Frame] sent via this socket. */
    private val ratelimitChannel = Channel<Frame>()

    /** A [Channel] used to send [Heartbeats][HeartbeatPayload] without ratelimiting. */
    private val directChannel = Channel<Frame>()

    /** Maximum number of heartbeat requests that can be made over a [RATELIMIT_PERIOD]. */
    private var heartbeatRequests = 0

    /**
     * Connects to the given [uri] and executes [onReceive] for every received [Frame] until the connection is closed.
     * Returns a [CloseReason] once the connection is closed, or null if it wasn't closed with a code.
     */
    @UseExperimental(FlowPreview::class)
    suspend fun connect(uri: String, onReceive: (CoroutineScope, String) -> Unit): CloseReason? {
        var reason: CloseReason? = null

        client.wss(host = uri.removePrefix("wss://")) {
            consumeRatelimitChannel(outgoing)
            consumeDirectChannel(outgoing)

            val scope = CoroutineScope(coroutineContext + SupervisorJob())

            incoming.consumeAsFlow().collect {
                if (it is Frame.Text) onReceive(scope, it.readText())
                else if (it is Frame.Close) logger.warn("Socket closed by remote server.")
            }

            ratelimitChannel.cancel()
            directChannel.cancel()

            reason = closeReason.await()
        }

        return reason
    }

    /** Sets the [heartbeatInterval], allowing the socket to calculate [heartbeatRequests] based on it. */
    fun setHeartbeatInterval(heartbeatInterval: Long) {
        heartbeatRequests = RATELIMIT_PERIOD.div(heartbeatInterval).toInt().plus(1)
    }

    /**
     * Serializes and sends the given [Payload] to [ratelimitChannel], or [directChannel] if it is a
     * [HeartbeatPayload].
     */
    @UseExperimental(UnstableDefault::class)
    suspend fun <T : Payload> send(serializer: KSerializer<T>, obj: T) {
        (if (obj is HeartbeatPayload) directChannel else ratelimitChannel)
            .takeUnless { it.isClosedForSend }
            ?.send(Frame.Text(Json.stringify(serializer, obj)))
    }

    /** Closes this WebSocket session. */
    suspend fun close(reason: CloseReason) {
        directChannel.takeUnless { it.isClosedForSend }?.send(Frame.Close(reason))
    }

    /** Consumes [ratelimitChannel] and sends its frames to [outgoing] with ratelimiting. */
    private suspend fun consumeRatelimitChannel(outgoing: SendChannel<Frame>) {
        CoroutineScope(coroutineContext).launch {
            var ratelimitUsed = 0
            var ratelimitReset = 0L

            ratelimitChannel.consumeAsFlow().collect { frame ->
                if (ratelimitReset - DateTime.nowUnixLong() < 0)
                    ratelimitUsed = 0

                outgoing.takeUnless { it.isClosedForSend }?.send(frame)?.also { ratelimitUsed++ }

                when (ratelimitUsed) {
                    1 -> ratelimitReset = DateTime.nowUnixLong() + RATELIMIT_PERIOD
                    RATELIMIT_REQUESTS.minus(heartbeatRequests) -> {
                        ratelimitReset.minus(DateTime.nowUnixLong())
                            .takeIf { it > 0 }
                            ?.also { logger.warn("Hit gateway ratelimit.") }
                            ?.also { delay(it) }
                    }
                }
            }
        }
    }

    /** Consumes [directChannel] and sends its frames to [outgoing] without ratelimiting. */
    private suspend fun consumeDirectChannel(outgoing: SendChannel<Frame>) {
        CoroutineScope(coroutineContext).launch {
            directChannel.consumeAsFlow().collect { frame -> outgoing.takeUnless { it.isClosedForSend }?.send(frame) }
        }
    }

    companion object {
        /** Time in milliseconds for how long the ratelimit lasts. */
        private const val RATELIMIT_PERIOD = 60_000L

        /** Maximum number of requests that can made over a [RATELIMIT_PERIOD]. */
        private const val RATELIMIT_REQUESTS = 120
    }
}
