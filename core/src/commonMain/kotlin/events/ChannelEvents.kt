package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User
import com.soywiz.klock.DateTime

interface ChannelEvent : Event {
    val channel: Channel?
}

class ChannelCreateEvent internal constructor(
    override val context: Context,
    override val channel: Channel
) : ChannelEvent

class ChannelUpdateEvent internal constructor(
    override val context: Context,
    override val channel: Channel
) : ChannelEvent

class ChannelDeleteEvent internal constructor(
    override val context: Context,
    val channelID: Long
) : ChannelEvent {
    override val channel: Channel? = null
}

class ChannelPinsUpdateEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel
) : ChannelEvent

class TypingStartEvent internal constructor(
    override val context: Context,
    override val channel: TextChannel,
    val user: User,
    val timestamp: DateTime
) : ChannelEvent
