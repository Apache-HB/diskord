package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.network.payloads.DispatchPayload
import com.serebit.loggerkt.Logger

internal class EventDispatcher(
    private val eventListeners: Set<EventListener>,
    private inline val contextProvider: () -> Context
) {
    suspend fun dispatch(dispatch: DispatchPayload) = dispatch.asEvent(contextProvider())?.let { event ->
        eventListeners.asSequence()
            .filter { it.eventType == event::class }
            .forEach { it.invoke(event) }
        Logger.trace("Dispatched event $event")
    }
}
