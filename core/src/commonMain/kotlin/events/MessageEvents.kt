package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Message
import com.serebit.strife.internal.dispatches.MessageDelete
import com.serebit.strife.internal.entitydata.add
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Endpoint.GetTextChannel
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.serebit.strife.internal.runBlocking

// TODO Make GenericMessageEvent and extract channelData caching process

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    private val channelData = context.getTextChannelData(packet.channel_id)
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!
    val message = packet.toData(context).also { channelData.messages.add(it) }.toEntity()
    val channel = channelData.toEntity()
}

/**
 * Received when a [Message] is updated.
 *
 * @constructor Builds a [MessageUpdatedEvent] from a [PartialMessagePacket]
 */
class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    // Attempt to get the channel data from cache, if not cached, send new request for data
    private val channelData = context.getTextChannelData(packet.channel_id)
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!
    val message = channelData.messages[packet.id]!!.also { it.update(packet) }.toEntity()
    val channel = channelData.toEntity()
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageID = packet.id
    val channel = context.getTextChannelData(packet.channel_id)?.toEntity()
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!.toEntity()

    init {
        context.getTextChannelData(packet.channel_id)?.messages?.remove(packet.id)
    }
}
