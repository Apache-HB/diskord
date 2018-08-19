package com.serebit.diskord.entities.channels

import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.CreateMessage
import com.serebit.diskord.internal.packets.TextChannelPacket

interface TextChannel : Channel {
    fun send(message: String) = Requester.requestObject(CreateMessage(id), data = mapOf("content" to message))

    companion object {
        internal fun from(packet: TextChannelPacket): TextChannel {
            return EntityCache.find(packet.id) ?: when (packet.type) {
                GuildTextChannel.typeCode -> GuildTextChannel(packet)
                DmChannel.typeCode -> DmChannel(packet)
                GroupDmChannel.typeCode -> GroupDmChannel(packet)
                else -> throw IllegalArgumentException("Unknown channel type with code ${packet.type} received.")
            }
        }

        fun find(id: Long) = Channel.find(id) as? TextChannel
    }
}
