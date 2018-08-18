package com.serebit.diskord.internal

import com.serebit.diskord.Context
import com.serebit.diskord.internal.network.payloads.DispatchPayload
import com.serebit.loggerkt.Logger

internal class EventDispatcher(private val eventListeners: Set<EventListener>, private val context: Context) {
    suspend fun dispatch(dispatch: DispatchPayload) = dispatch.asEvent(context)?.let { event ->
        eventListeners.asSequence()
            .filter { it.eventType == event::class }
            .forEach { it.invoke(event) }
        Logger.trace("Dispatched event $event")
    }
}
