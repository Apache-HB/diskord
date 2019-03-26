package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User
import com.soywiz.klock.DateTime

/** An [Event] based around a [Channel]. */
interface ChannelEvent : Event {
    val channel: Channel
}

/**
 * Received when a new [Channel] is created.
 *
 * @property channel The created [Channel].
 */
class ChannelCreateEvent internal constructor(
    override val context: Context,
    override val channel: Channel
) : ChannelEvent

/**
 * Received when a [Channel] is updated. TODO More specific docs.
 *
 * @property channel The updated [Channel].
 */
class ChannelUpdateEvent internal constructor(
    override val context: Context,
    override val channel: Channel
) : ChannelEvent

/**
 * Received when a [Channel] is deleted.
 *
 * @property channelID The [id][Channel.id] of the deleted [Channel].
 */
class ChannelDeleteEvent internal constructor(override val context: Context, val channelID: Long) : Event

/**
 * Received when a [Message] is (un)pinned in a [TextChannel].
 *
 * @property channel The [TextChannel] with its [TextChannel.lastPinTime] updated.
 */
class ChannelPinsUpdateEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel
) : ChannelEvent

/**
 * Received when a [User] starts typing in a [TextChannel].
 *
 * @property user The [User] who is typing.
 * @property channel The [TextChannel] in which the [user] is typing.
 * @property timestamp The [DateTime] of the [TypingStartEvent].
 */
class TypingStartEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel,
    val user: User,
    val timestamp: DateTime
) : ChannelEvent
