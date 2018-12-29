package com.serebit.strife.internal

import com.serebit.strife.events.Event
import kotlin.reflect.KClass

internal inline fun <reified T : Event> eventListener(noinline function: suspend (T) -> Any) =
    eventListener(T::class, function)

@Suppress("UNCHECKED_CAST")
internal fun <T : Event> eventListener(eventType: KClass<T>, function: suspend (T) -> Any) =
    EventListener(eventType) {
        function(it as T)
        Unit
    }

internal class EventListener(val eventType: KClass<out Event>, private val function: suspend (Event) -> Unit) {
    suspend operator fun invoke(evt: Event) = function(evt)
}
