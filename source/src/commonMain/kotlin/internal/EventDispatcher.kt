package com.serebit.diskord.internal

import com.serebit.diskord.Context
import com.serebit.diskord.internal.payloads.DispatchPayload
import com.serebit.logkat.Logger

internal class EventDispatcher(
    private val eventListeners: Set<EventListener>,
    private val logger: Logger
) {
    suspend fun dispatch(dispatch: DispatchPayload, context: Context) {
        dispatch.asEvent(context)?.let { event ->
            eventListeners.asSequence()
                .filter { it.eventType.isInstance(event) }
                .forEach { it(event) }
            logger.trace("Dispatched ${event::class.simpleName}.")
        }
    }
}
