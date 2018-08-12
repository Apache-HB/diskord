package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.packets.GuildVoiceChannelPacket

class GuildVoiceChannel internal constructor(packet: GuildVoiceChannelPacket) : Channel {
    override val id = packet.id
    val guild: Guild? = packet.guild_id?.let { EntityCache.find(it) }
    var name = packet.name
        private set
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
        private set
    var position: Int = packet.position
        private set
    var permissionOverwrites: Nothing = TODO("implement this")
        private set
    var bitrate: Int = packet.bitrate
        private set
    var userLimit: Int = packet.user_limit
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 2
    }
}
