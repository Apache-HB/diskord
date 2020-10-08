package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.logkat.warn
import com.serebit.strife.internal.HeartbeatPayload
import com.serebit.strife.internal.Payload
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext

/**
 * Provides an interface to connect to Discord gateways. This class handles ratelimiting and gives a
 * special exception to [Heartbeats][HeartbeatPayload] to pass without being ratelimited. Once connected, it
 * will take care of the connection until it is closed, hiding all its internals from [Gateway].
 */
@OptIn(
    ExperimentalCoroutinesApi::class, FlowPreview::class
)
internal class GatewaySocket(private val logger: Logger) {
    /** A default [HttpClient] with [WebSockets] feature installed. */
    @OptIn(KtorExperimentalAPI::class)
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
    @OptIn(FlowPreview::class)
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
    suspend fun <T : Payload> send(serializer: KSerializer<T>, obj: T) {
        (if (obj is HeartbeatPayload) directChannel else ratelimitChannel)
            .takeUnless { it.isClosedForSend }
            ?.send(Frame.Text(json.encodeToString(serializer, obj)))
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
                if (ratelimitReset - Clock.System.now().toEpochMilliseconds() < 0)
                    ratelimitUsed = 0

                outgoing.takeUnless { it.isClosedForSend }?.send(frame)?.also { ratelimitUsed++ }

                when (ratelimitUsed) {
                    1 -> ratelimitReset = Clock.System.now().toEpochMilliseconds() + RATELIMIT_PERIOD
                    RATELIMIT_REQUESTS.minus(heartbeatRequests) -> {
                        ratelimitReset.minus(Clock.System.now().toEpochMilliseconds())
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

        private val json = Json { encodeDefaults = true }
    }
}
