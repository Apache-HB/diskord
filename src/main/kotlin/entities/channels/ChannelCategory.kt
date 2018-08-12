package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.packets.ChannelCategoryPacket

class ChannelCategory internal constructor(packet: ChannelCategoryPacket) : Channel {
    override val id = packet.id
    var guild: Guild? = packet.guild_id?.let { EntityCache.find(it)!! }
        private set
    var name: String = packet.name
        private set
    var position = packet.position
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 4
    }
}
