package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.Channel

data class ChannelCreatedEvent internal constructor(override val context: Context, val channel: Channel) : Event
