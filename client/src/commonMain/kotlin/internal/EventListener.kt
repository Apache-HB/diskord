package com.serebit.strife.internal

import com.serebit.strife.events.Event
import kotlin.reflect.KType

internal class EventListener<T : Event>(
    val eventType: KType,
    private inline val function: suspend (T) -> Unit
) {
    @Suppress("unchecked_cast")
    suspend operator fun invoke(evt: Event) = function(evt as T)
}
