package com.serebit.diskord.entities.channels

import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket

class ChannelCategory internal constructor(packet: ChannelCategoryPacket) : GuildChannel {
    override val id = packet.id
    override var guild: Guild? = packet.guild_id?.let { EntityCache.find(it)!! }
        private set
    override var name: String = packet.name
        private set
    override var position = packet.position
        private set
    override val permissionOverwrites: Nothing get() = TODO("not implemented")

    internal constructor(packet: GuildChannelPacket) : this(
        ChannelCategoryPacket(
            packet.id, packet.type, packet.guild_id, packet.name, packet.parent_id, packet.nsfw, packet.position,
            packet.permission_overwrites
        )
    )

    internal constructor(packet: ChannelPacket) : this(
        ChannelCategoryPacket(
            packet.id, packet.type, packet.guild_id, packet.name!!, packet.parent_id, packet.nsfw, packet.position!!,
            packet.permission_overwrites!!
        )
    )

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 4

        fun find(id: Long) = Channel.find(id) as? ChannelCategory
    }
}
