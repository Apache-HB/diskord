package com.serebit.diskord.internal

import com.serebit.diskord.Context
import com.serebit.diskord.internal.payloads.DispatchPayload
import com.serebit.loggerkt.Logger

internal class EventDispatcher(private val eventListeners: Set<EventListener>, private val context: Context) {
    suspend fun dispatch(dispatch: DispatchPayload) = dispatch.asEvent(context)?.let { event ->
        val eventClass = event::class
        eventListeners.asSequence()
            .filter { it.eventType == eventClass }
            .forEach { it(event) }
        Logger.trace("Dispatched ${eventClass.simpleName}.")
    }
}
