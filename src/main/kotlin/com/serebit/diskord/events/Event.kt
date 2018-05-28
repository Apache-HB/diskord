package com.serebit.diskord.events

import com.serebit.diskord.entities.Channel
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.entities.User

sealed class Event

data class ReadyEvent internal constructor(val user: User) : Event()

data class GuildCreatedEvent internal constructor(val guild: Guild) : Event()

data class MessageCreatedEvent internal constructor(val channel: Channel, val author: User) : Event()


