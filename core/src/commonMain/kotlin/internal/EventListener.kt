package com.serebit.strife.internal

import com.serebit.strife.events.Event
import kotlin.reflect.KClass

internal class EventListener<T : Event>(
    val eventType: KClass<T>,
    private inline val function: suspend (T) -> Unit
) {
    @Suppress("unchecked_cast")
    suspend operator fun invoke(evt: Event) {
        check(eventType.isInstance(evt))
        function(evt as T)
    }
}
