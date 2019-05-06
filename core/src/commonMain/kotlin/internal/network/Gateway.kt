package com.serebit.strife.internal.network

import com.serebit.strife.internal.*
import com.serebit.strife.internal.dispatches.Unknown
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.wss
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json

/**
 * [Gateways][Gateway] are Discord's form of real-time communication over secure websockets.
 * The client receives events and data over the [Gateway] they are connected to and send
 * data over the REST API. The API for interacting with Gateways is complex and fairly
 * unforgiving, therefore it's highly recommended you read all of the
 * [documentation.](https://discordapp.com/developers/docs/topics/gateway#gateways)
 */
@UseExperimental(KtorExperimentalAPI::class)
internal class Gateway(private val uri: String, private val sessionInfo: SessionInfo) {
    private val client = HttpClient { install(WebSockets) }
    private var session: WebSocketSession? = null

    private var lastSequence: Int = 0
    private var sessionID: String? = null

    private var heart = Heart(sessionInfo.logger) {
        send(HeartbeatPayload.serializer(), HeartbeatPayload(lastSequence))
    }
    private val handler = CoroutineExceptionHandler { _, throwable ->
        sessionInfo.logger.error("Error in gateway: ${throwable.stackTraceAsString}")
    }

    /** Attempt to connect the [Gateway] to its specified remote URI. */
    @UseExperimental(ObsoleteCoroutinesApi::class)
    suspend fun connect(onDispatch: suspend (CoroutineScope, DispatchPayload) -> Unit) {
        check(session == null) { "Connect method called on active socket" }

        client.wss(host = uri.removePrefix("wss://")) {
            session = this

            val scope = CoroutineScope(coroutineContext + SupervisorJob())

            incoming.mapNotNull { it as? Frame.Text }.consumeEach {
                scope.launch(handler) {
                    when (val payload = Payload(it.readText())) {
                        is HelloPayload -> {
                            heart.interval = payload.d.heartbeat_interval
                            sessionID?.let { id -> resumeSession(id) } ?: openNewSession()
                            heart.start(scope, ::disconnect)
                        }
                        is HeartbeatPayload -> heart.beat()
                        is HeartbeatAckPayload -> heart.acknowledge()
                        is DispatchPayload -> if (payload is Unknown) {
                            sessionInfo.logger.trace("Received unknown dispatch with type ${payload.t}")
                        } else {
                            onDispatch(scope, payload)
                        }
                    }
                }
            }
        }

        onProcessExit {
            disconnect()
        }
    }

    @UseExperimental(UnstableDefault::class)
    suspend fun <T : Payload> send(serializer: KSerializer<T>, obj: T) = session.let {
        checkNotNull(it) { "Send method called on inactive socket" }

        it.send(Frame.Text(Json.stringify(serializer, obj)))
    }

    suspend fun disconnect() = session.let {
        checkNotNull(it) { "Disconnect method called on inactive socket" }

        heart.kill()
        it.close(CloseReason(CloseReason.Codes.NORMAL, "Normal close"))
    }

    suspend fun updateStatus(payload: StatusUpdatePayload) {
        send(StatusUpdatePayload.serializer(), payload)
    }

    private suspend fun resumeSession(sessionID: String) {
        send(ResumePayload.serializer(), ResumePayload(sessionInfo.token, sessionID, lastSequence))
    }

    private suspend fun openNewSession() {
        send(IdentifyPayload.serializer(), IdentifyPayload(sessionInfo.identification))
    }
}


internal enum class GatewayCloseCode(val code: Short, val message: String, val action: PostCloseAction) {
    GRACEFUL_CLOSE(1000, "The connection was closed gracefully or your heartbeats timed out.", PostCloseAction.CLOSE),
    CLOUD_FLARE_LOAD(1001, "The connection was closed due to CloudFlare load balancing.", PostCloseAction.RESTART),
    INTERNAL_SERVER_ERROR(1006, "Something broke on the remote server's end.", PostCloseAction.RESTART),
    UNKNOWN_ERROR(4000, "The server is not sure what went wrong.", PostCloseAction.RESUME),
    UNKNOWN_OPCODE(4001, "You sent an invalid gateway op code.", PostCloseAction.RESUME),
    DECODE_ERROR(4002, "We sent an invalid payload to the server.", PostCloseAction.RESUME),
    NOT_AUTHENTICATED(4003, "We sent a payload prior to identifying.", PostCloseAction.RESTART),
    AUTH_FAILED(4004, "The account token sent with our identify payload is incorrect.", PostCloseAction.CLOSE),
    ALREADY_AUTHENTICATED(4005, "We sent more than one identify payload.", PostCloseAction.RESUME),
    INVALID_SEQ(4007, "The payload sent when resuming the session was invalid.", PostCloseAction.RESTART),
    RATE_LIMITED(4008, "You're sending payloads to us too quickly.", PostCloseAction.RESUME),
    SESSION_TIMEOUT(4009, "Your session timed out. Reconnect and start a new one.", PostCloseAction.RESTART),
    INVALID_SHARD(4010, "You sent an invalid shard when identifying.", PostCloseAction.CLOSE),
    SHARD_REQUIRED(4011, "You are required to shard in order to connect.", PostCloseAction.CLOSE);
}

internal enum class PostCloseAction { RESUME, RESTART, CLOSE }
