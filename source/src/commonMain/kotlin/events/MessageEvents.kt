package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.toTextChannel
import com.serebit.diskord.entities.toMessage
import com.serebit.diskord.findTextChannelInCaches
import com.serebit.diskord.internal.caching.add
import com.serebit.diskord.internal.dispatches.MessageDelete
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.network.Endpoint
import com.serebit.diskord.internal.packets.MessageCreatePacket
import com.serebit.diskord.internal.packets.PartialMessagePacket

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    private val channelData = context.findTextChannelInCaches(packet.channel_id)
        ?: context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id))
            .returned
            ?.toTypedPacket()
            ?.toData(context)!!
    val message = packet.toData(context).also {
        channelData.messages.add(it)
    }.toMessage()
    val channel = channelData.toTextChannel()
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    private val channelData = context.findTextChannelInCaches(packet.channel_id)
        ?: context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id))
            .returned
            ?.toTypedPacket()
            ?.toData(context)!!
    val message = channelData.messages[packet.id]!!.also {
        it.update(packet)
    }.toMessage()
    val channel = channelData.toTextChannel()
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageId = packet.id
    val channel = context.findTextChannelInCaches(packet.channel_id)?.toTextChannel()
        ?: context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id)).returned
            ?.toTypedPacket()
            ?.toData(context)!!.toTextChannel()

    init {
        context.findTextChannelInCaches(packet.channel_id)?.messages?.remove(packet.id)
    }
}
