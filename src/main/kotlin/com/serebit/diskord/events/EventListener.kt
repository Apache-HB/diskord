package com.serebit.diskord.events

import kotlin.reflect.KClass

internal class EventListener(val eventType: KClass<out Event>, private val function: (Event) -> Unit) {
    operator fun invoke(evt: Event) = function(evt)
}
