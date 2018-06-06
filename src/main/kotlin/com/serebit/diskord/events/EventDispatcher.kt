package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.gateway.Payload

internal class EventDispatcher(
    private val eventListeners: Set<EventListener>,
    private inline val contextProvider: () -> Context
) {
    fun dispatch(dispatch: Payload.Dispatch) = dispatch.asEvent(contextProvider())?.let { event ->
        eventListeners
            .filter { it.eventType == event::class }
            .forEach { it.invoke(event) }
    }
}
