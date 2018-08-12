package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.EntityCache
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.User
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.network.Requester
import com.serebit.diskord.network.endpoints.GetChannel
import com.serebit.diskord.network.endpoints.GetUser
import com.serebit.diskord.network.payloads.DispatchPayload
import java.time.Instant

class TypingStartEvent internal constructor(
    override val context: Context,
    payload: DispatchPayload.TypingStart
) : Event {
    val user: User = EntityCache.find(payload.d.user_id)
        ?: Requester.requestObject(GetUser(payload.d.user_id))
        ?: throw EntityNotFoundException("No user with ID ${payload.d.user_id} found.")
    val channel: TextChannel = EntityCache.find(payload.d.channel_id)
        ?: Requester.requestObject(GetChannel(payload.d.channel_id)) as? TextChannel
        ?: throw EntityNotFoundException("No channel with ID ${payload.d.channel_id} found.")
    val timestamp = Instant.ofEpochSecond(payload.d.timestamp)
}
