package com.serebit.diskord.internal.network

import com.serebit.diskord.internal.payloads.DispatchPayload
import com.serebit.diskord.internal.payloads.HeartbeatAckPayload
import com.serebit.diskord.internal.payloads.HeartbeatPayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

internal class Heart(private val socket: Socket) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var job: Job? = null
    private var lastSequence: Int = 0
    private var state = State.DEAD

    fun start(rate: Long, onDeath: () -> Unit) {
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
        socket.send(HeartbeatPayload(lastSequence))
        state = State.AWAITING_ACK
        Logger.trace("Sent heartbeat.")
    }

    enum class State {
        DEAD, AWAITING_ACK, RESTING
    }
}
