package com.serebit.diskord.internal.network

import com.serebit.diskord.Context
import com.serebit.diskord.internal.DispatchPayload
import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.HelloPayload
import com.serebit.diskord.internal.IdentifyPayload
import com.serebit.diskord.internal.ResumePayload
import com.serebit.diskord.internal.dispatches.Ready
import com.serebit.diskord.internal.dispatches.Unknown
import com.serebit.diskord.internal.runBlocking
import com.serebit.logkat.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

internal class Gateway(
    uri: String,
    private val sessionInfo: SessionInfo,
    private val logger: Logger,
    listeners: Set<EventListener>
) {
    private val socket = Socket(uri)
    private var lastSequence: Int = 0
    private var sessionId: String? = null
    private var heart = Heart(socket, logger)
    private val context = Context(Requester(sessionInfo, logger), ::disconnect)
    private val eventDispatcher = EventDispatcher(listeners, logger)

    fun connect(): HelloPayload? = runBlocking {
        suspendCoroutineWithTimeout<HelloPayload>(connectionTimeout) { continuation ->
            socket.onPayload { payload ->
                (payload as? HelloPayload)?.let { continuation.resume(it) }
            }
            socket.connect()
        }
    }

    fun disconnect() = runBlocking {
        socket.close(GatewayCloseCode.GRACEFUL_CLOSE)
        withTimeout(connectionTimeout) {
            while (socket.isOpen) delay(payloadPollDelay)
        }
    }

    fun openSession(hello: HelloPayload): Ready? = runBlocking {
        suspendCoroutineWithTimeout<Ready>(connectionTimeout) {
            socket.onPayload { payload ->
                if (payload is DispatchPayload) {
                    if (payload is Ready) {
                        Context.selfUserId = payload.d.user.id
                        it.resume(payload)
                    }
                    processDispatch(payload)
                }
            }
            sessionId?.let { id -> resumeSession(hello, id) } ?: openNewSession(hello)
        }
    }

    private fun resumeSession(hello: HelloPayload, sessionId: String) {
        startHeartbeat(hello.d.heartbeat_interval)
        socket.send(ResumePayload.serializer(), ResumePayload(sessionInfo.token, sessionId, lastSequence))
    }

    private fun openNewSession(hello: HelloPayload) {
        startHeartbeat(hello.d.heartbeat_interval)
        socket.send(IdentifyPayload.serializer(), IdentifyPayload(sessionInfo.identification))
    }

    private fun startHeartbeat(interval: Long) = heart.start(interval, ::disconnect)

    private suspend fun processDispatch(dispatch: DispatchPayload) {
        if (dispatch !is Unknown) {
            eventDispatcher.dispatch(dispatch, context)
        } else logger.trace("Received unknown dispatch with type ${dispatch.t}")
    }

    private suspend inline fun <T> suspendCoroutineWithTimeout(
        timeout: Long,
        crossinline block: (Continuation<T>) -> Unit
    ) = withTimeoutOrNull(timeout) {
        suspendCancellableCoroutine(block = block)
    }

    companion object {
        private const val connectionTimeout = 20000L // ms
        private const val payloadPollDelay = 50L // ms
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
