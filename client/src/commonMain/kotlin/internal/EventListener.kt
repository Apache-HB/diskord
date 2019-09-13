package com.serebit.strife.internal

import com.serebit.strife.events.Event
import kotlin.reflect.KType

/** An Enumeration used to denote when an event listener's task function has either run successfully or failed.*/
enum class EventResult {
    /** Indicates that the task succeeded. */
    SUCCESS,
    /** Indicates that the task failed. */
    FAILURE
}

/**
 * An [EventListener] is used to consume dispatched API events in a customized manner.
 *
 * @param T The Event type to be consumed
 * @property eventType The event type to be consumed
 */
internal sealed class EventListener<T : Event>(val eventType: KType) {

    /** Whether the [EventListener] is active or set for disposal. */
    enum class ListenerState { ACTIVE, TERMINATED }

    /** The current state of the Listener. */
    var state = ListenerState.ACTIVE

    open suspend operator fun invoke(event: Event) = Unit
}

/**
 * An [IndefiniteEventListener] will consume the given EventType, [T], as long as the client is connected.
 *
 * @property function The task function to invoke on an event dispatch.
 */
internal class IndefiniteEventListener<T : Event>(eventType: KType, private val function: suspend (T) -> Unit) :
    EventListener<T>(eventType) {

    @Suppress("UNCHECKED_CAST")
    override suspend fun invoke(event: Event) = function(event as T)
}

/**
 * A [TerminableEventListener] will consume the given Event Type, [T], until it has successfully run its [function]
 * [successRunLimit] times.
 *
 * @property successRunLimit The number of successful runs of the [function] before the listener is set for disposal.
 * Must be greater 0.
 * @property function The task function to invoke on an event dispatch.
 */
internal class TerminableEventListener<T : Event>(
    eventType: KType,
    private val successRunLimit: Int = 1,
    private val function: suspend (T) -> EventResult
) : EventListener<T>(eventType) {

    init {
        require(successRunLimit > 0) { "Terminable Event Listener: successRunLimit must be greater than 0" }
    }

    private var runCount = 0

    @Suppress("UNCHECKED_CAST")
    override suspend fun invoke(event: Event) {
        if (function(event as T) == EventResult.SUCCESS && ++runCount >= successRunLimit)
            state = ListenerState.TERMINATED
    }

}
