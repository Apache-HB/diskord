package com.serebit.diskord.entities.channels

import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

class GuildVoiceChannel internal constructor(packet: GuildVoiceChannelPacket) : GuildChannel {
    override val id = packet.id
    override val guild: Guild? = packet.guild_id?.let { EntityCache.find(it) }
    override var name = packet.name
        private set
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
        private set
    override var position: Int = packet.position
        private set
    override var permissionOverwrites: Nothing = TODO("implement this")
        private set
    var bitrate: Int = packet.bitrate
        private set
    var userLimit: Int = packet.user_limit
        private set

    internal constructor(packet: GuildChannelPacket) : this(
        GuildVoiceChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position, packet.permission_overwrites, packet.name,
            packet.nsfw, packet.bitrate!!, packet.user_limit!!, packet.parent_id
        )
    )

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 2
    }
}
