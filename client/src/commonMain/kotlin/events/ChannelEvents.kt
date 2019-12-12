package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User
import com.soywiz.klock.DateTime

/** An [Event] based around a [Channel]. */
interface ChannelEvent : Event {
    /** The relevant [Channel]. */
    val channel: Channel?
}

/**
 * Received when a new [Channel] is created.
 *
 * @property channel The created [Channel].
 */
class ChannelCreateEvent internal constructor(
    override val context: BotClient,
    override val channel: Channel
) : ChannelEvent

/**
 * Received when a [Channel] is updated.
 *
 * @property channel The updated [Channel].
 */
class ChannelUpdateEvent internal constructor(
    override val context: BotClient,
    override val channel: Channel
) : ChannelEvent

/**
 * Received when a [Channel] is deleted.
 *
 * @property channelID The ID of the deleted [Channel].
 * @property channel The deleted [Channel]. This may be `null` if the [Channel] was not in cache at the time of the
 * event.
 */
class ChannelDeleteEvent internal constructor(
    override val context: BotClient,
    override val channel: Channel?,
    val channelID: Long
) : ChannelEvent

/**
 * Received when a [Message] is (un)pinned in a [TextChannel].
 *
 * @property channel The [TextChannel] with its [TextChannel.getLastPinTime] updated.
 */
class ChannelPinsUpdateEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel
) : ChannelEvent

/**
 * Received when a [User] starts typing in a [TextChannel].
 *
 * @property user The [User] who is typing.
 * @property channel The [TextChannel] in which the [user] is typing.
 * @property timestamp When the user started typing.
 */
class TypingStartEvent internal constructor(
    override val context: BotClient,
    override val channel: TextChannel,
    val user: User,
    val timestamp: DateTime
) : ChannelEvent
