package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.network.Payload
import com.serebit.loggerkt.Logger

internal class EventDispatcher(
    private val eventListeners: Set<EventListener>,
    private inline val contextProvider: () -> Context
) {
    suspend fun dispatch(dispatch: Payload.Dispatch) = dispatch.asEvent(contextProvider())?.let { event ->
        eventListeners.asSequence()
            .filter { it.eventType == event::class }
            .forEach { it.invoke(event) }
        Logger.trace("Dispatched event $event")
    }
}
