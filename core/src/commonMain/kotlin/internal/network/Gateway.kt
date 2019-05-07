package com.serebit.strife.internal.network

import com.serebit.strife.internal.*
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.dispatches.Resumed
import com.serebit.strife.internal.dispatches.Unknown
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.random.nextLong

/**
 * [Gateways][Gateway] are Discord's form of real-time communication over secure websockets.
 * The client receives events and data over the [Gateway] they are connected to and send
 * data over the REST API. The API for interacting with Gateways is complex and fairly
 * unforgiving, therefore it's highly recommended you read all of the
 * [documentation.](https://discordapp.com/developers/docs/topics/gateway#gateways)
 */
@UseExperimental(KtorExperimentalAPI::class)
internal class Gateway(
    private val uri: String,
    private val sessionInfo: SessionInfo,
    private val listener: GatewayListener
) {
    private val client = HttpClient { install(WebSockets) }
    private var session: DefaultWebSocketSession? = null

    private var sessionID: String? = null
    private var lastSequence: Int = 0

    private var heart = Heart(sessionInfo.logger) {
        send(HeartbeatPayload.serializer(), HeartbeatPayload(lastSequence))
    }
    private val handler = CoroutineExceptionHandler { _, throwable ->
        sessionInfo.logger.error("Error in gateway: ${throwable.stackTraceAsString}")
    }

    /** Attempt to connect the [Gateway] to its specified remote URI. */
    suspend fun connect() {
        onProcessExit {
            disconnect()
        }

        coroutineScope {
            sessionInfo.logger.info("Connecting to Discord...")
            maintainConnection()
        }
    }

    /**
     * A tail-recursive function that maintains the connection to Discord until [disconnect] is called or we receive
     * a [PostCloseAction.CLOSE].
     */
    private tailrec suspend fun maintainConnection() {
        val closeReason = openConnection()

        val closeCode = GatewayCloseCode.values().firstOrNull { it.code == closeReason?.code }
        sessionInfo.logger.error(
            "Got disconnected: ${closeReason?.code ?: "No code: "} ${closeCode?.message ?: "Unknown reason."}"
        )

        if (closeReason?.message?.startsWith("<CLIENT>") == true) {
            sessionInfo.logger.info("Connection closed by client.")
        } else when (closeCode?.action) {
            PostCloseAction.RESUME, null -> {
                sessionInfo.logger.info("Attempting to resume...")
                maintainConnection()
            }
            PostCloseAction.RESTART -> {
                sessionID = null
                sessionInfo.logger.info("Attempting to reconnect...")
                maintainConnection()
            }
            PostCloseAction.CLOSE -> {
                sessionInfo.logger.info("No further attempts to reconnect.")
            }
        }
    }

    @UseExperimental(UnstableDefault::class)
    suspend fun <T : Payload> send(serializer: KSerializer<T>, obj: T) = session.let {
        checkNotNull(it) { "Send method called on inactive socket" }

        it.send(Frame.Text(Json.stringify(serializer, obj)))
    }

    suspend fun disconnect() = session?.also {
        heart.kill()
        it.close(CloseReason(CloseReason.Codes.NORMAL, "<CLIENT> Normal close."))
    } ?: sessionInfo.logger.warn("Attempted to disconnect an inactive gateway.")

    suspend fun updateStatus(payload: StatusUpdatePayload) {
        send(StatusUpdatePayload.serializer(), payload)
    }

    /**
     * Returns the close reason of the opened session, or null if no close reason was given.
     */
    @UseExperimental(ObsoleteCoroutinesApi::class)
    private suspend fun openConnection(): CloseReason? {
        check(session == null) { "openConnection method called on active socket" }

        var closeReason: CloseReason? = null

        client.wss(host = uri.removePrefix("wss://")) {
            session = this
            val scope = CoroutineScope(coroutineContext + SupervisorJob())

            incoming.consumeEach {
                if (it is Frame.Text) onReceive(scope, it.readText())
                else if (it is Frame.Close) sessionInfo.logger.warn("Socket closed by remote server.")
            }

            closeReason = this@wss.closeReason.await()
            session = null
        }

        return closeReason
    }

    private fun onReceive(scope: CoroutineScope, frameText: String) = scope.launch(handler) {
        when (val payload = Payload(frameText)) {
            is HelloPayload -> {
                sessionID?.also { resumeSession(it) } ?: openNewSession()
                heart
                    .apply { interval = payload.d.heartbeat_interval }
                    .start(scope) {
                        session?.close(GatewayCloseCode.HEARTBEAT_EXPIRED.let { CloseReason(it.code, it.message) })
                    }
            }
            is InvalidSessionPayload -> {
                sessionID?.takeIf { payload.d }?.also { resumeSession(it) } ?: also {
                    sessionInfo.logger.info("Invalid session. Starting a new one...")

                    delay(Random.nextLong(1L..5L))
                    openNewSession()
                }
            }
            is HeartbeatPayload -> heart.beat()
            is HeartbeatAckPayload -> heart.acknowledge()
            is DispatchPayload -> {
                when (payload) {
                    is Unknown -> sessionInfo.logger.trace("Received unknown dispatch with type ${payload.t}")
                    is Ready -> {
                        sessionInfo.logger.info("Successfully started session.")

                        sessionID = payload.d.session_id
                    }
                    is Resumed -> sessionInfo.logger.info("Successfully resumed session.")
                }

                lastSequence = payload.s
                listener.onDispatch(scope, payload)
            }
        }
    }

    private suspend fun resumeSession(sessionID: String) {
        send(ResumePayload.serializer(), ResumePayload(sessionInfo.token, sessionID, lastSequence))
    }

    private suspend fun openNewSession() {
        send(IdentifyPayload.serializer(), IdentifyPayload(sessionInfo.identification))
    }
}

internal enum class GatewayCloseCode(val code: Short, val message: String, val action: PostCloseAction) {
    GRACEFUL_CLOSE(1000, "The connection was closed gracefully.", PostCloseAction.RESUME),
    CLOUD_FLARE_LOAD(
        1001, "The connection was closed due to CloudFlare load balancing.",
        PostCloseAction.RESTART
    ),
    INTERNAL_SERVER_ERROR(1006, "Something broke on the remote server's end.", PostCloseAction.RESTART),
    HEARTBEAT_EXPIRED(1008, "The heartbeat has expired.", PostCloseAction.RESUME),
    UNKNOWN_ERROR(4000, "The server is not sure what went wrong.", PostCloseAction.RESUME),
    UNKNOWN_OPCODE(4001, "You sent an invalid gateway op code.", PostCloseAction.RESUME),
    DECODE_ERROR(4002, "We sent an invalid payload to the server.", PostCloseAction.RESUME),
    NOT_AUTHENTICATED(4003, "We sent a payload prior to identifying.", PostCloseAction.RESTART),
    AUTH_FAILED(
        4004, "The account token sent with our identify payload is incorrect.",
        PostCloseAction.CLOSE
    ),
    ALREADY_AUTHENTICATED(4005, "We sent more than one identify payload.", PostCloseAction.RESUME),
    INVALID_SEQ(
        4007, "The payload sent when resuming the session was invalid.",
        PostCloseAction.RESTART
    ),
    RATE_LIMITED(4008, "You're sending payloads to us too quickly.", PostCloseAction.RESUME),
    SESSION_TIMEOUT(
        4009, "Your session timed out. Reconnect and start a new one.",
        PostCloseAction.RESTART
    ),
    INVALID_SHARD(4010, "You sent an invalid shard when identifying.", PostCloseAction.CLOSE),
    SHARD_REQUIRED(4011, "You are required to shard in order to connect.", PostCloseAction.CLOSE);
}

internal enum class PostCloseAction { RESUME, RESTART, CLOSE }
