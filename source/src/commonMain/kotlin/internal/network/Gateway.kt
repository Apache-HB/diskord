package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.Context
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.IdentifyPayload
import com.serebit.strife.internal.ResumePayload
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.runBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class Gateway(uri: String, private val sessionInfo: SessionInfo, logger: Logger) {
    private val socket = Socket(uri)
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var heart = Heart(socket, logger)

    suspend fun connect(): HelloPayload? = suspendCoroutineWithTimeout(CONNECTION_TIMEOUT) { continuation ->
        socket.onHelloPayload { continuation.resume(it) }
        socket.connect()
    }

    suspend fun disconnect() {
        heart.kill()
        socket.close(GatewayCloseCode.GRACEFUL_CLOSE)
        withTimeout(CONNECTION_TIMEOUT) {
            while (socket.isOpen) delay(PAYLOAD_POLL_DELAY)
        }
    }

    suspend fun openSession(hello: HelloPayload, onSuccess: suspend () -> Unit) =
        suspendCoroutineWithTimeout<Ready>(CONNECTION_TIMEOUT) {
            socket.onReadyDispatch { payload ->
                Context.selfUserId = payload.d.user.id
                onSuccess()
                it.resume(payload)
            }
            runBlocking { sessionId?.let { id -> resumeSession(hello, id) } ?: openNewSession(hello) }
        }

    fun onDispatch(callback: suspend (DispatchPayload) -> Unit) = socket.onPayload {
        if (it is DispatchPayload) callback(it)
    }

    private suspend fun resumeSession(hello: HelloPayload, sessionId: String) {
        socket.send(ResumePayload.serializer(), ResumePayload(sessionInfo.token, sessionId, lastSequence))
        startHeartbeat(hello.d.heartbeat_interval)
    }

    private suspend fun openNewSession(hello: HelloPayload) {
        socket.send(IdentifyPayload.serializer(), IdentifyPayload(sessionInfo.identification))
        startHeartbeat(hello.d.heartbeat_interval)
    }

    private suspend fun startHeartbeat(interval: Long) = heart.start(interval, ::disconnect)

    private suspend inline fun <T> suspendCoroutineWithTimeout(
        timeout: Long,
        crossinline block: (Continuation<T>) -> Unit
    ) = withTimeoutOrNull(timeout) {
        suspendCancellableCoroutine(block = block)
    }

    companion object {
        private const val CONNECTION_TIMEOUT = 20000L // ms
        private const val PAYLOAD_POLL_DELAY = 50L // ms
    }
}

internal enum class GatewayCloseCode(val code: Int, val message: String, val action: PostCloseAction) {
    GRACEFUL_CLOSE(1000, "The connection was closed gracefully or your heartbeats timed out.", PostCloseAction.CLOSE),
    CLOUD_FLARE_LOAD(1001, "The connection was closed due to CloudFlare load balancing.", PostCloseAction.RESTART),
    INTERNAL_SERVER_ERROR(1006, "Something broke on the remote server's end.", PostCloseAction.RESTART),
    UNKNOWN_ERROR(4000, "The server is not sure what went wrong.", PostCloseAction.RESUME),
    UNKNOWN_OPCODE(4001, "You sent an invalid gateway op code.", PostCloseAction.RESUME),
    DECODE_ERROR(4002, "We sent an invalid payload to the server.", PostCloseAction.RESUME),
    NOT_AUTHENTICATED(4003, "We sent a payload prior to identifying.", PostCloseAction.RESTART),
    AUTHENTICATION_FAILED(
        4004, "The account token sent with our identify payload is incorrect.", PostCloseAction.CLOSE
    ),
    ALREADY_AUTHENTICATED(4005, "We sent more than one identify payload.", PostCloseAction.RESUME),
    INVALID_SEQ(4007, "The payload sent when resuming the session was invalid.", PostCloseAction.RESTART),
    RATE_LIMITED(4008, "You're sending payloads to us too quickly.", PostCloseAction.RESUME),
    SESSION_TIMEOUT(4009, "Your session timed out. Reconnect and start a new one.", PostCloseAction.RESTART),
    INVALID_SHARD(4010, "You sent an invalid shard when identifying.", PostCloseAction.CLOSE),
    SHARDING_REQUIRED(
        4011,
        "The session would have handled too many guilds - you are required to shard in order to connect.",
        PostCloseAction.CLOSE
    );
}

internal enum class PostCloseAction {
    RESUME, RESTART, CLOSE
}
