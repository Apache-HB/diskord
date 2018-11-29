package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.entities.channels.TextChannel
import com.serebit.diskord.entities.channels.toChannel
import com.serebit.diskord.entities.channels.toTextChannel
import com.serebit.diskord.entities.toMessage
import com.serebit.diskord.findTextChannelInCaches
import com.serebit.diskord.internal.caching.add
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket
import com.serebit.diskord.internal.payloads.dispatches.MessageDelete

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    private val channelData = context.findTextChannelInCaches(packet.channel_id)!!
    val message = packet.toData(context).also {
        channelData.messages.add(it)
    }.toMessage()
    val channel = channelData.toTextChannel()
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    private val channelData = context.findTextChannelInCaches(packet.channel_id)!!
    val message = channelData.messages[packet.id]!!.also {
        it.update(packet)
    }.toMessage()
    val channel = channelData.toTextChannel()
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = context.findTextChannelInCaches(packet.channel_id)!!.toTextChannel()

    init {
        context.findTextChannelInCaches(packet.channel_id)?.messages?.remove(packet.id)
    }
}
