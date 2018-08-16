package com.serebit.diskord.network

import com.neovisionaries.ws.client.WebSocket
import com.serebit.diskord.network.payloads.DispatchPayload
import com.serebit.diskord.network.payloads.HeartbeatAckPayload
import com.serebit.diskord.network.payloads.HeartbeatPayload
import com.serebit.loggerkt.Logger
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

internal class Heart(private val socket: WebSocket) {
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
