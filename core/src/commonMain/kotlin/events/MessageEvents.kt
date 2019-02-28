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
import com.serebit.strife.entities.TextChannel

/** Received when a [Message] is sent in a [TextChannel]. */
class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    private val channelData = context.getTextChannelData(packet.channel_id)
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!
    /** The newly created [Message]. */
    val message = packet.toData(context).also { channelData.messages.add(it) }.toEntity()
    /** The [TextChannel] the [Message] was sent in. */
    val channel = channelData.toEntity()
}

/** Received when a [Message] is updated. */
class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    // Attempt to get the channel data from cache, if not cached, send new request for data
    private val channelData = context.getTextChannelData(packet.channel_id)
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!
    /** The newly created [Message]. */
    val message = channelData.messages[packet.id]!!.also { it.update(packet) }.toEntity()
    /** The [TextChannel] the [Message] was sent in. */
    val channel = channelData.toEntity()
}

/** Received when a [Message] is deleted.*/
class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    /** The [ID][Message.id] of the deleted [Message]. */
    val messageID = packet.id
    val channel = context.getTextChannelData(packet.channel_id)?.toEntity()
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(packet.channel_id)) }
            .value
            ?.toTypedPacket()
            ?.toData(context)!!.toEntity()

    init {
        // Remove the Message from the cached channel
        context.getTextChannelData(packet.channel_id)?.messages?.remove(packet.id)
    }
}
