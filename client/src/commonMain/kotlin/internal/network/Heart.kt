package com.serebit.strife.internal.network

import com.serebit.logkat.Logger
import kotlinx.coroutines.*
import kotlin.time.*

internal class Heart(private val logger: Logger, private inline val onBeat: suspend () -> Unit) {
    var interval = 0L
    var state = State.DEAD
    private var job: Job? = null
    /** The Discord Websocket API connection latency */
    @UseExperimental(ExperimentalTime::class)
    private var latency: Duration = 0.toDuration(DurationUnit.MILLISECONDS)
    /** The ClockMark for keeping track of the Websocket connection latency*/
    @UseExperimental(ExperimentalTime::class)
    private var clockMark: ClockMark? = null

    /** Gets the current Gateway connection latency */
    fun getLatency() = latency

    suspend fun start(scope: CoroutineScope, onDeath: suspend () -> Unit) {
        state = State.DEAD
        job?.cancelAndJoin()
        job = scope.launch {
            while (state != State.AWAITING_ACK) {
                beat()
                delay(interval)
            }
            job = null
            onDeath()
        }
    }

    suspend fun kill() {
        job?.cancelAndJoin()
        job = null
    }

    @UseExperimental(ExperimentalTime::class)
    fun acknowledge() {
        if (state == State.AWAITING_ACK) state = State.RESTING
        logger.trace("Received acknowledge.")
        latency = clockMark?.elapsedNow() ?: 0.toDuration(DurationUnit.MILLISECONDS)
    }

    @UseExperimental(ExperimentalTime::class)
    suspend fun beat() {
        onBeat()
        state = State.AWAITING_ACK
        logger.trace("Sent heartbeat.")
        clockMark = MonoClock.markNow()
    }

    enum class State {
        DEAD, AWAITING_ACK, RESTING
    }
}
