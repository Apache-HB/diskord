package com.serebit.diskord

import com.serebit.diskord.events.MessageCreatedEvent
import com.serebit.loggerkt.LogLevel
import com.serebit.loggerkt.Logger

fun main(args: Array<String>) {
    Logger.level = LogLevel.TRACE
    diskord(args[0]) {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.author.isNormalUser) {
                evt.message.reply("Message sent from ${evt.message.author.username}.").await()
            }
        }
    }
}