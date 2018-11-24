package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete
import com.serebit.diskord.internal.removeMessage

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    val message = Message(packet.id, packet.channel_id, context)
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        context.cache.cache(packet.toData(context))
    }
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    val message = Message(packet.id, packet.channel_id, context)
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        context.cache.update(packet)
    }
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        context.cache.removeMessage(packet.id, packet.channel_id)
    }
}
