package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetChannel
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

interface Channel : Entity {
    companion object {
        internal fun from(packet: ChannelPacket): Channel = when (packet) {
            is GuildTextChannelPacket -> GuildTextChannel(packet.id)
            is GuildVoiceChannelPacket -> GuildVoiceChannel(packet.id)
            is ChannelCategoryPacket -> ChannelCategory(packet.id)
            is DmChannelPacket -> DmChannel(packet.id)
            is GroupDmChannelPacket -> GroupDmChannel(packet.id)
            else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of ${packet.type}.")
        }

        fun find(id: Long): Channel? {
            val packet = EntityPacketCache.findId(id)
                ?: Requester.requestObject(GetChannel(id))?.toTypedPacket()
                ?: return null
            return when (packet) {
                is GuildTextChannelPacket -> GuildTextChannel(packet.id)
                is GuildVoiceChannelPacket -> GuildVoiceChannel(packet.id)
                is ChannelCategoryPacket -> ChannelCategory(packet.id)
                is DmChannelPacket -> DmChannel(packet.id)
                is GroupDmChannelPacket -> GroupDmChannel(packet.id)
                else -> null
            }
        }
    }
}
