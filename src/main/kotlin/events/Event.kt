package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel

interface Event {
    val context: Context
}

data class ReadyEvent internal constructor(override val context: Context, val user: User) : Event

data class GuildCreatedEvent internal constructor(override val context: Context, val guild: Guild) : Event

data class MessageCreatedEvent internal constructor(override val context: Context, val message: Message) : Event

data class ChannelCreatedEvent internal constructor(override val context: Context, val channel: Channel) : Event

class UnknownEvent internal constructor(override val context: Context) : Event
