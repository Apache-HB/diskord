package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel

/**
 * Received when a [Message] is sent in a [TextChannel].
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The newly created [Message].
 */
class MessageCreatedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val message: Message
) : Event

/**
 * Received when a [Message] is updated.
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The [Message] which was updated
 */
class MessageUpdatedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val message: Message
) : Event

/**
 * Received when a [Message] is deleted.
 *
 * @property messageID The [ID][Message.id] of the deleted [Message].
 * @property channel The [TextChannel] the [Message] was deleted from.
 */
class MessageDeletedEvent internal constructor(
    override val context: Context,
    val channel: TextChannel,
    val messageID: Long
) : Event
