package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Emoji
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User

/** An [Event] involving one or more [Messages][Message]. */
interface MessageEvent : Event {
    /** The [TextChannel] of the [message]. */
    val channel: TextChannel
}

/** An [Event] involving a single [Message]. */
interface SingleMessageEvent : MessageEvent {
    /** The relevant [Message]. */
    val message: Message?
    /** The message's ID, in case the message is null. */
    val messageID: Long
}

/**
 * Received when a [Message] is sent in a [TextChannel].
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The newly created [Message].
 */
class MessageCreateEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message,
    override val messageID: Long
) : SingleMessageEvent

/**
 * Received when a [Message] is updated.
 *
 * @property channel The [TextChannel] the [Message] was sent in.
 * @property message The [Message] which was updated
 */
class MessageEditEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message,
    override val messageID: Long
) : SingleMessageEvent

/** Received when a [Message] is deleted. */
class MessageDeleteEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    override val messageID: Long
) : SingleMessageEvent

/**
 * Received when multiple [Messages][Message] are deleted at the same time.
 *
 * @property messages A map of the deleted [Messages][Message]. If a [Message] was not found in cache,
 * its ID will map to null.
 */
class MessageBulkDeleteEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    val messages: Map<Long, Message?>
) : MessageEvent

/** Received when a [user] reacts on a [message] with an [emoji]. */
class MessageReactionAddEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    override val messageID: Long,
    /** The user whose reaction was removed. */
    val user: User?,
    /** The [user]'s ID, in case [user] is null. */
    val userID: Long,
    /** The emoji corresponding to the removed reaction. */
    val emoji: Emoji
) : SingleMessageEvent

/** Received when a [user]'s reaction with an [emoji] was removed from a [message] by the user or moderators. */
class MessageReactionRemoveEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    override val messageID: Long,
    /** The user whose reaction was removed. */
    val user: User?,
    /** The [user]'s ID, in case [user] is null. */
    val userID: Long,
    /** The emoji corresponding to the removed reaction. */
    val emoji: Emoji
) : SingleMessageEvent

/** Received when all reactions were removed from a [message]. */
class MessageReactionRemoveAllEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    override val message: Message?,
    override val messageID: Long
) : SingleMessageEvent
