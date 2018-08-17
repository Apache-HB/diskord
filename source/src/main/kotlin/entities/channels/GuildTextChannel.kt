package com.serebit.diskord.entities.channels

import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.TextChannelPacket

class GuildTextChannel internal constructor(packet: GuildTextChannelPacket) : TextChannel, GuildChannel {
    override val id = packet.id
    override var name = packet.name
        private set
    override val guild: Guild? = packet.guild_id?.let { EntityCache.find(it) }
    override var position = packet.position
        private set
    override val permissionOverwrites: Nothing get() = TODO("implement this")
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
    var topic: String = packet.topic ?: ""
        private set
    var isNsfw: Boolean = packet.nsfw ?: false
        private set

    internal constructor(packet: TextChannelPacket) : this(
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!,
            packet.name!!, packet.topic!!, packet.nsfw!!, packet.last_message_id!!, packet.parent_id!!,
            packet.last_pin_timestamp!!
        )
    )

    internal constructor(packet: GuildChannelPacket) : this(
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position, packet.permission_overwrites,
            packet.name, packet.topic, packet.nsfw, packet.last_message_id, packet.parent_id,
            packet.last_pin_timestamp
        )
    )

    internal constructor(packet: ChannelPacket) : this(
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!,
            packet.name!!, packet.topic, packet.nsfw, packet.last_message_id, packet.parent_id,
            packet.last_pin_timestamp
        )
    )

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 0
    }
}