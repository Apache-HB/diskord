package com.serebit.diskord.events

import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.Channel

interface Event

data class ReadyEvent internal constructor(val user: User) : Event

data class GuildCreatedEvent internal constructor(val guild: Guild) : Event

data class MessageCreatedEvent internal constructor(val message: Message) : Event

data class ChannelCreatedEvent internal constructor(val channel: Channel) : Event

class UnknownEvent internal constructor() : Event
