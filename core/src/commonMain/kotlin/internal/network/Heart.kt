package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import kotlinx.coroutines.*

internal class Heart(private val logger: Logger, private inline val onBeat: suspend () -> Unit) {
    var interval = 0L
    var state = State.DEAD
    private var job: Job? = null

    suspend fun start(scope: CoroutineScope, onDeath: suspend () -> Unit) {
        state = State.DEAD
        job?.cancelAndJoin()
        job = scope.launch {
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
        onBeat()
        state = State.AWAITING_ACK
        logger.trace("Sent heartbeat.")
    }

    enum class State {
        DEAD, AWAITING_ACK, RESTING
    }
}
