package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.packets.GuildChannelPacket

interface GuildChannel : Channel {

    companion object {
        internal fun from(packet: GuildChannelPacket): GuildChannel =
            EntityCache.find(packet.id) ?: when (packet.type) {
                GuildTextChannel.typeCode -> GuildTextChannel(packet)
                GuildVoiceChannel.typeCode -> GuildVoiceChannel(packet)
                ChannelCategory.typeCode -> ChannelCategory(packet)
                else -> throw IllegalArgumentException("Unknown channel type with code ${packet.type} received.")
            }
    }
}
