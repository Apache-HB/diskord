package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.HeartbeatAckPayload
import com.serebit.strife.internal.HeartbeatPayload
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class Heart(private val socket: Socket, private val logger: Logger) {
    private var job: Job? = null
    private var lastSequence: Int = 0
    private var state = State.DEAD

    suspend fun start(rate: Long, onDeath: suspend () -> Unit) = coroutineScope {
        socket.onPayload { payload ->
            when (payload) {
                is HeartbeatPayload -> beat()
                is HeartbeatAckPayload -> state = State.RESTING
                is DispatchPayload -> lastSequence = payload.s
            }
        }
        state = State.DEAD
        job = launch {
            while (state != State.AWAITING_ACK) {
                beat()
                delay(rate)
            }
            onDeath()
            job = null
        }
    }

    fun kill() {
        job?.cancel()
        job = null
    }

    private fun beat() {
        socket.send(HeartbeatPayload.serializer(), HeartbeatPayload(lastSequence))
        state = State.AWAITING_ACK
        logger.trace("Sent heartbeat.")
    }

    enum class State {
        DEAD, AWAITING_ACK, RESTING
    }
}
