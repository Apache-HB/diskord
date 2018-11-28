package com.serebit.diskord.entities.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.UnknownEntityTypeException
import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.entitydata.channels.ChannelData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.network.endpoints.GetChannel
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

interface Channel : Entity {
    companion object {
        internal fun from(packet: ChannelPacket, context: Context): Channel = when (packet) {
            is GuildTextChannelPacket -> GuildTextChannel(packet.id, context)
            is GuildVoiceChannelPacket -> GuildVoiceChannel(packet.id, context)
            is ChannelCategoryPacket -> ChannelCategory(packet.id, context)
            is DmChannelPacket -> DmChannel(packet.id, context)
            is GroupDmChannelPacket -> GroupDmChannel(packet.id, context)
            else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of ${packet.type}.")
        }

        fun find(id: Long, context: Context): Channel? {
            val packet = EntityPacketCache.findId(id)
                ?: context.requester.requestObject(GetChannel(id))?.toTypedPacket()
                ?: return null
            return when (packet) {
                is GuildTextChannelPacket -> GuildTextChannel(packet.id, context)
                is GuildVoiceChannelPacket -> GuildVoiceChannel(packet.id, context)
                is ChannelCategoryPacket -> ChannelCategory(packet.id, context)
                is DmChannelPacket -> DmChannel(packet.id, context)
                is GroupDmChannelPacket -> GroupDmChannel(packet.id, context)
                else -> null
            }
        }
    }
}

internal fun ChannelData.toChannel() = when (this) {
    is GuildChannelData -> toChannel()
    is DmChannelData -> toChannel()
    is GroupDmChannelData -> toChannel()
    else -> throw UnknownEntityTypeException("Unknown ChannelData type passed to toChannel function.")
}
