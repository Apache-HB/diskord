package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.MessageCreatedEvent
import com.serebit.strife.events.MessageDeletedEvent
import com.serebit.strife.events.MessageUpdatedEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.MessageCreatePacket
import com.serebit.strife.internal.packets.PartialMessagePacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.serebit.strife.internal.set
import kotlinx.serialization.Serializable

private suspend fun obtainChannelData(id: Long, context: Context) = context.cache.getTextChannelData(id)
    ?: context.requester.sendRequest(Route.GetChannel(id))
        .value
        ?.let { it.toTypedPacket() as GuildTextChannelPacket }
        ?.toData(context)

@Serializable
internal class MessageCreate(override val s: Int, override val d: MessageCreatePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context): MessageCreatedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val message = d.toData(context).also { channelData.messages[it.id] = it }.toEntity()

        return MessageCreatedEvent(context, channelData.toEntity(), message)
    }
}

@Serializable
internal class MessageUpdate(override val s: Int, override val d: PartialMessagePacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context): MessageUpdatedEvent? {
        val channelData = obtainChannelData(d.channel_id, context) ?: return null
        val message = channelData.messages[d.id]?.also { it.update(d) }?.toEntity()

        return message?.let { MessageUpdatedEvent(context, channelData.toEntity(), it) }
    }
}

@Serializable
internal class MessageDelete(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): MessageDeletedEvent? {
        val channelData = obtainChannelData(d.channel_id, context)
            ?.also { it.messages.remove(d.id) }
            ?: return null
        val message = channelData.messages[d.id]?.toEntity()

        return MessageDeletedEvent(context, channelData.toEntity(), message, d.id)
    }

    @Serializable
    data class Data(val id: Long, val channel_id: Long)
}
