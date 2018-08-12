package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.packets.GuildTextChannelPacket

class GuildTextChannel internal constructor(packet: GuildTextChannelPacket) : TextChannel {
    override val id = packet.id
    val guild: Guild? = packet.guild_id?.let { EntityCache.find(it) }
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
    val permissionOverwrites: Nothing get() = TODO("implement this")
    var name = packet.name
        private set
    var position = packet.position
        private set
    var topic = packet.topic ?: ""
        private set
    var isNsfw = packet.nsfw
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 0
    }
}
