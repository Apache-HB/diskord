package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Guild

data class GuildCreatedEvent internal constructor(override val context: Context, val guild: Guild) : Event
