package com.serebit.strife.internal.network

import com.serebit.strife.internal.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer

internal class Gateway(uri: String, private val sessionInfo: SessionInfo) {
    private val socket = Socket(uri)
    private var lastSequence: Int = 0
    private var sessionID: String? = null
    private var heart = Heart(socket, sessionInfo.logger)
    private val handler = CoroutineExceptionHandler { _, throwable -> sessionInfo.logger.error(throwable.toString()) }

    suspend fun connect(onDispatch: suspend (CoroutineScope, DispatchPayload) -> Unit) {
        socket.connect { scope, it ->
            scope.launch(handler) {
                when (val payload = Payload.from(it)) {
                    is HelloPayload -> {
                        heart.interval = payload.d.heartbeat_interval
                        sessionID?.let { id -> resumeSession(id) } ?: openNewSession()
                        heart.start(::disconnect)
                    }
                    is HeartbeatPayload -> heart.beat()
                    is HeartbeatAckPayload -> heart.acknowledge()
                    is DispatchPayload -> onDispatch(scope, payload)
                }
            }
        }
    }

    suspend fun disconnect() {
        heart.kill()
        socket.close(GatewayCloseCode.GRACEFUL_CLOSE)
    }

    private suspend fun resumeSession(sessionID: String) {
        socket.send(ResumePayload.serializer(), ResumePayload(sessionInfo.token, sessionID, lastSequence))
    }

    private suspend fun openNewSession() {
        socket.send(IdentifyPayload.serializer(), IdentifyPayload(sessionInfo.identification))
    }
}

internal expect class Socket(uri: String) {
    suspend fun connect(onReceive: suspend (CoroutineScope, String) -> Unit)

    suspend fun send(text: String)

    suspend fun <T : Any> send(serializer: KSerializer<T>, obj: T)

    suspend fun close(code: GatewayCloseCode = GatewayCloseCode.GRACEFUL_CLOSE)
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
