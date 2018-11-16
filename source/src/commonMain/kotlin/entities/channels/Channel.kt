package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetChannel
import com.serebit.diskord.internal.packets.ChannelPacket

interface Channel : Entity {
    class Unknown internal constructor(packet: ChannelPacket) : Channel {
        override val id = packet.id
    }

    companion object {
        internal fun from(packet: ChannelPacket): Channel = when (packet.type) {
            GuildTextChannel.typeCode -> GuildTextChannel(packet)
            GuildVoiceChannel.typeCode -> GuildVoiceChannel(packet)
            ChannelCategory.typeCode -> ChannelCategory(packet)
            DmChannel.typeCode -> DmChannel(packet.asDmChannelPacket.cache().id)
            GroupDmChannel.typeCode -> GroupDmChannel(packet)
            else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of ${packet.type}.")
        }

        fun find(id: Long): Channel? = EntityCache.findId(id)
            ?: Requester.requestObject(GetChannel(id))?.let { Channel.from(it).cache() }
    }
}
