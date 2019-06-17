package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Emoji
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User


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

/** Received when a [user] reacts on a [message] with an [emoji]. */
class MessageReactionAddedEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    val messageID: Long,
    val user: User?,
    val userID: Long,
    val emoji: Emoji
) : MessageEvent

/** Received when a [user]'s reaction with an [emoji] was removed from a [message] by the user or moderators. */
class MessageReactionRemovedEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    val messageID: Long,
    val user: User?,
    val userID: Long,
    val emoji: Emoji
) : MessageEvent

/** Received when all reactions were removed from a [message]. */
class MessageReactionRemovedAllEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    val messageID: Long
) : MessageEvent