package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.channels.toTextChannel
import com.serebit.strife.entities.toMessage
import com.serebit.strife.findTextChannelInCaches
import com.serebit.strife.internal.caching.add
import com.serebit.strife.internal.dispatches.MessageDelete
import com.serebit.strife.internal.entitydata.channels.toData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Endpoint
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.serebit.strife.internal.runBlocking

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    private val channelData = context.findTextChannelInCaches(packet.channel_id)
        ?: runBlocking { context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id)) }
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
        ?: runBlocking { context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id)) }
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
        ?: runBlocking { context.requester.sendRequest(Endpoint.GetTextChannel(packet.channel_id)) }
            .returned
            ?.toTypedPacket()
            ?.toData(context)!!.toTextChannel()

    init {
        context.findTextChannelInCaches(packet.channel_id)?.messages?.remove(packet.id)
    }
}
