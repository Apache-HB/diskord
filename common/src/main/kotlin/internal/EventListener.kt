package com.serebit.diskord.internal

import com.serebit.diskord.events.Event
import kotlin.reflect.KClass

internal class EventListener(val eventType: KClass<out Event>, private val function: suspend (Event) -> Unit) {
    suspend operator fun invoke(evt: Event) = function(evt)
}
