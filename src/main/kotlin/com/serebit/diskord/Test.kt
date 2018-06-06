package com.serebit.diskord

import com.serebit.diskord.events.MessageCreatedEvent

const val version = "0.0.0"

fun main(args: Array<String>) {
    diskord(args[0]) {
        onEvent { evt: MessageCreatedEvent ->
            if (evt.message.author.isNormalUser) {
                evt.message.reply("Message sent from ${evt.message.author.username}. ${evt.message.author.avatar}")
            }
        }
    }
}
