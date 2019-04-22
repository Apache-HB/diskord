package com.serebit.strife.commands

import com.serebit.strife.BotBuilder
import com.serebit.strife.BotBuilderDsl
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.onMessage

@BotBuilderDsl
fun BotBuilder.command(name: String, task: suspend MessageCreatedEvent.() -> Unit) = onMessage {
    if (message.content == "!$name") task()
}
