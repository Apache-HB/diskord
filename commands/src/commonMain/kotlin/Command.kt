package com.serebit.strife.commands

import com.serebit.strife.events.MessageCreatedEvent

internal data class Command(
    val name: String,
    val paramTypes: List<ParamType>,
    private val task: suspend (MessageCreatedEvent, List<Any>) -> Unit
) {
    val signature = buildString {
        append("(\\Q$name\\E)")
        if (paramTypes.isNotEmpty()) append(" ${paramTypes.signature}")
    }.toRegex()

    suspend operator fun invoke(event: MessageCreatedEvent, params: List<Any>) = task(event, params)
}
