package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.HeartbeatPayload
import kotlinx.coroutines.*

internal class Heart(private val socket: Socket, private val logger: Logger) {
    var interval = 0L
    var state = State.DEAD
    var lastSequence = 0
    private var job: Job? = null

    suspend fun start(onDeath: suspend () -> Unit) = coroutineScope {
        state = State.DEAD
        job?.cancelAndJoin()
        job = launch {
            while (state != State.AWAITING_ACK) {
                beat()
                delay(interval)
            }
            onDeath()
            job = null
        }
    }

    suspend fun kill() {
        job?.cancelAndJoin()
        job = null
    }

    fun acknowledge() {
        if (state == State.AWAITING_ACK) state = State.RESTING
    }

    suspend fun beat() {
        socket.send(HeartbeatPayload.serializer(), HeartbeatPayload(lastSequence))
        state = State.AWAITING_ACK
        logger.trace("Sent heartbeat.")
    }

    enum class State {
        DEAD, AWAITING_ACK, RESTING
    }
}
