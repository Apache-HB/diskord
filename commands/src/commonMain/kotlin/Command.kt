package com.serebit.strife.commands

import com.serebit.strife.events.MessageCreateEvent

internal data class Command(
    val name: String,
    val paramTypes: List<ParamType<*>>,
    private val task: suspend (MessageCreateEvent, List<Any>) -> Unit
) {
    val signature = buildString {
        append("(\\Q$name\\E)")
        if (paramTypes.isNotEmpty()) append(" ${paramTypes.signature}")
    }.toRegex()

    suspend operator fun invoke(event: MessageCreateEvent, params: List<Any>) = task(event, params)
}
