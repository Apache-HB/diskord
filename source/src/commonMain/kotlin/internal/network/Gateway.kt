package com.serebit.diskord.internal.network

import com.serebit.diskord.Context
import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.payloads.DispatchPayload
import com.serebit.diskord.internal.payloads.HelloPayload
import com.serebit.diskord.internal.payloads.IdentifyPayload
import com.serebit.diskord.internal.payloads.ResumePayload
import com.serebit.diskord.internal.payloads.dispatches.Ready
import com.serebit.diskord.internal.payloads.dispatches.Unknown
import com.serebit.diskord.internal.runBlocking
import com.serebit.logkat.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

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
        var helloPayload: HelloPayload? = null
        socket.onPayload { payload ->
            helloPayload = payload as? HelloPayload
        }
        socket.connect()
        withTimeoutOrNull(connectionTimeout) {
            while (helloPayload == null) delay(payloadPollDelay)
            socket.clearListeners()
        }
        helloPayload
    }

    fun disconnect() = runBlocking {
        socket.close(GatewayCloseCode.GRACEFUL_CLOSE)
        withTimeout(connectionTimeout) {
            while (socket.isOpen) delay(payloadPollDelay)
        }
    }

    fun openSession(hello: HelloPayload): Ready? = runBlocking {
        var readyPayload: Ready? = null
        socket.onPayload { payload ->
            if (payload is DispatchPayload) {
                if (payload is Ready) {
                    Context.selfUserId = payload.d.user.id
                    readyPayload = payload
                }
                processDispatch(payload)
            }
        }
        sessionId?.let { resumeSession(hello, it) } ?: openNewSession(hello)
        withTimeout(connectionTimeout) {
            while (readyPayload == null) delay(payloadPollDelay)
        }
        readyPayload
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

    companion object {
        private const val connectionTimeout = 20000L // ms
        private const val payloadPollDelay = 50L // ms
    }
}
