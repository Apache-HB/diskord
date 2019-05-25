package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel


/** An [Event] involving a [Message]. */
interface MessageEvent : Event {
    /** The [TextChannel] of the [message]. */
    val channel: TextChannel
    /** The relevant [Message]. */
    val message: Message?
}

/**
 * Received when a [Message] is sent in a [TextChannel].
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The newly created [Message].
 */
class MessageCreatedEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message
) : MessageEvent

/**
 * Received when a [Message] is updated.
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The [Message] which was updated
 */
class MessageUpdatedEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message
) : MessageEvent

/**
 * Received when a [Message] is deleted.
 *
 * @property messageID The [ID][Message.id] of the deleted [Message].
 * @property channel The [TextChannel] the [Message] was deleted from.
 */
class MessageDeletedEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    val messageID: Long
) : MessageEvent
