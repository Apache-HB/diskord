package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.payloads.dispatches.TypingStart
import java.time.Instant

class TypingStartEvent internal constructor(
    override val context: Context,
    payload: TypingStart
) : Event {
    val user = User.find(payload.d.user_id)
        ?: throw EntityNotFoundException("No user with ID ${payload.d.user_id} found.")
    val channel = Channel.find(payload.d.channel_id) as? TextChannel
        ?: throw EntityNotFoundException("No channel with ID ${payload.d.channel_id} found.")
    val timestamp = Instant.ofEpochSecond(payload.d.timestamp)
}
