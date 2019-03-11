package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel

class MessageCreatedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val message: Message
) : Event

class MessageUpdatedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val message: Message
) : Event

class MessageDeletedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val messageID: Long
) : Event
