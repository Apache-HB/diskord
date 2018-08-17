package com.serebit.diskord.entities.channels

import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.loggerkt.Logger

interface GuildChannel : Channel {
    val guild: Guild?
    val position: Int
    val name: String
    val permissionOverwrites: Nothing

    class Unknown internal constructor(packet: GuildChannelPacket) : GuildChannel {
        override val id = packet.id
        override val name: String = packet.name
        override val guild: Guild? = packet.guild_id?.let { EntityCache.find(it) }
        override val position: Int = packet.position
        override val permissionOverwrites: Nothing get() = TODO("implement this")
    }

    companion object {
        internal fun from(packet: GuildChannelPacket): GuildChannel =
            EntityCache.find(packet.id) ?: when (packet.type) {
                GuildTextChannel.typeCode -> GuildTextChannel(packet)
                GuildVoiceChannel.typeCode -> GuildVoiceChannel(packet)
                ChannelCategory.typeCode -> ChannelCategory(packet)
                else -> {
                    Logger.warn("Received a channel with an unknown typecode of ${packet.type}.")
                    Unknown(packet)
                }
            }
    }
}
