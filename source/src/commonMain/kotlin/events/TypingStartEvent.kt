package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.DateTime
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.payloads.dispatches.TypingStart

class TypingStartEvent internal constructor(
    override val context: Context,
    payload: TypingStart
) : Event {
    val user = User(payload.d.user_id)
    val channel = Channel.find(payload.d.channel_id) as? TextChannel
        ?: throw EntityNotFoundException("No channel with ID ${payload.d.channel_id} found.")
    val timestamp = DateTime.fromUnixTimestamp(payload.d.timestamp)
}
