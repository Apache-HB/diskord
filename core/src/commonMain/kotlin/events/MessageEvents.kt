package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.internal.dispatches.MessageDelete
import com.serebit.strife.internal.entitydata.ChannelData
import com.serebit.strife.internal.entitydata.TextChannelData
import com.serebit.strife.internal.entitydata.add
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Endpoint
import com.serebit.strife.internal.network.Endpoint.*
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.serebit.strife.internal.packets.TextChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.serebit.strife.internal.runBlocking

private val channelData: (Context, Long) -> TextChannelData<*, *>? = { context, id ->
    context.cache.getTextChannelData(id)
        ?: runBlocking { context.requester.sendRequest(GetTextChannel(id)) }
            .value
            ?.toTypedPacket()
            ?.let { context.cache.pushChannelData(it) } as? TextChannelData<*, *>
}

class MessageCreatedEvent internal constructor(override val context: Context, packet: MessageCreatePacket) : Event {
    val message = packet.toData(context).also { channelData(context, packet.channel_id)!!.messages.add(it) }.toEntity()
    val channel = channelData(context, packet.channel_id)!!.toEntity()
}

class MessageUpdatedEvent internal constructor(override val context: Context, packet: PartialMessagePacket) : Event {
    val message = channelData(context, packet.channel_id)!!.messages[packet.id]!!.also { it.update(packet) }.toEntity()
    val channel = channelData(context, packet.channel_id)!!.toEntity()
}

class MessageDeletedEvent internal constructor(override val context: Context, packet: MessageDelete.Data) : Event {
    val messageID = packet.id
    val channel = channelData(context, packet.channel_id)!!.toEntity()

    init {
        channelData(context, packet.channel_id)?.messages?.remove(packet.id)
    }
}
