package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel

interface MessageEvent : Event {
    val channel: TextChannel
    val message: Message
}

class MessageCreatedEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel,
    override val message: Message
) : MessageEvent

class MessageUpdatedEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel,
    override val message: Message
) : MessageEvent

class MessageDeletedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val messageID: Long
) : Event
