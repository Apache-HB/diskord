package com.serebit.diskord

import com.serebit.diskord.events.Event
import com.serebit.diskord.events.MessageCreatedEvent

object EventDispatcher {
    private val eventListeners: MutableSet<(Event) -> Unit> = mutableSetOf({ evt ->
        (evt as? MessageCreatedEvent)?.let {
            if (it.message.author.id != 450109042220859392L) it.message.channel.send("my name jeff")
        }
    })

    internal fun dispatch(event: Event) = eventListeners.forEach { it.invoke(event) }
}
