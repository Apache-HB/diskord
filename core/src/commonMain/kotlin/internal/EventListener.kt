package com.serebit.strife.internal

import com.serebit.strife.events.Event
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal fun <T : Event> eventListener(eventType: KClass<T>, function: suspend (T) -> Unit) =
    EventListener(eventType) { function(it as T) }

internal class EventListener(val eventType: KClass<out Event>, private val function: suspend (Event) -> Unit) {
    suspend operator fun invoke(evt: Event) = function(evt)
}
