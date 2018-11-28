package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.findChannelInCaches
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.findById
import com.serebit.diskord.internal.entitydata.removeById
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    val message = Message(packet.id, packet.channel_id, context)
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        (context.findChannelInCaches(packet.channel_id) as? TextChannelData)?.messages?.add(packet.toData(context))
    }
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    val message = Message(packet.id, packet.channel_id, context)
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        (context.findChannelInCaches(packet.channel_id) as? TextChannelData)
            ?.messages
            ?.findById(packet.id)
            ?.update(packet)
    }
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = Channel.find(packet.channel_id, context) as? TextChannel
        ?: throw EntityNotFoundException("No text channel with ID ${packet.channel_id} found.")

    init {
        (context.findChannelInCaches(packet.channel_id) as? TextChannelData)?.messages?.removeById(packet.id)
    }
}
