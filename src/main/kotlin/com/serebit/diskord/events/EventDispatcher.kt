package com.serebit.diskord.events

class EventDispatcher internal constructor(private val eventListeners: Set<EventListener>) {
    internal fun dispatch(event: Event) = eventListeners
        .filter { it.eventType == event::class }
        .forEach { it.invoke(event) }
}
