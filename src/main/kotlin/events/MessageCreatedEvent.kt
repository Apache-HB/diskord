package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Message

data class MessageCreatedEvent internal constructor(override val context: Context, val message: Message) : Event
