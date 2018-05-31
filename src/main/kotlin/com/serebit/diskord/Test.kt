package com.serebit.diskord

import com.serebit.diskord.events.MessageCreatedEvent

const val version = "0.0.0"

fun main(args: Array<String>) {
    diskord(args[0]) {
        listener { evt: MessageCreatedEvent ->
            if (evt.message.author.id != 450109042220859392L) {
                evt.message.channel.send("henlo owo")
            }
        }
    }
}
