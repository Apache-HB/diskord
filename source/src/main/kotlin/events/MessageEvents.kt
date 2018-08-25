package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.network.payloads.DispatchPayload.MessageDelete
import com.serebit.diskord.internal.packets.MessagePacket

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet).cache()
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: MessagePacket) : Event {
    val message = Message(packet).cache()
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id

    val channel = TextChannel.find(packet.channel_id)
        ?: throw EntityNotFoundException("No channel with ID ${packet.channel_id} found.")
}
