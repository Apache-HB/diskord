package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.entities.channels.toTextChannel
import com.serebit.diskord.entities.toUser
import com.serebit.diskord.findTextChannelInCaches
import com.serebit.diskord.internal.payloads.dispatches.TypingStart

class TypingStartEvent internal constructor(override val context: Context, data: TypingStart.Data) : Event {
    val user = context.userCache[data.user_id]!!.toUser()
    val channel = context.findTextChannelInCaches(data.channel_id)!!.toTextChannel()
    val timestamp = data.timestamp.toDateTime()
}
