package com.serebit.diskord.entities.channels

import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.*
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
        internal fun from(guild: Guild, packet: GuildChannelPacket): GuildChannel =
            EntityCache.find(packet.id) ?: when (packet.type) {
                GuildTextChannel.typeCode -> GuildTextChannel(guild, packet)
                GuildVoiceChannel.typeCode -> GuildVoiceChannel(guild, packet)
                ChannelCategory.typeCode -> ChannelCategory(guild, packet)
                else -> {
                    Logger.warn("Received a channel with an unknown typecode of ${packet.type}.")
                    Unknown(packet)
                }
            }

        internal fun find(id: Long) = Channel.find(id) as? GuildChannel
    }
}


class GuildTextChannel internal constructor(
    override val guild: Guild,
    packet: GuildTextChannelPacket
) : TextChannel, GuildChannel {
    override val id = packet.id
    override var name = packet.name
        private set
    override var position = packet.position
        private set
    override val permissionOverwrites: Nothing get() = TODO("implement this")
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
    var topic: String = packet.topic ?: ""
        private set
    var isNsfw: Boolean = packet.nsfw ?: false
        private set

    internal constructor(guild: Guild, packet: TextChannelPacket) : this(
        guild,
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!,
            packet.name!!, packet.topic!!, packet.nsfw!!, packet.last_message_id!!, packet.parent_id!!,
            packet.last_pin_timestamp!!
        )
    )

    internal constructor(guild: Guild, packet: GuildChannelPacket) : this(
        guild,
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position, packet.permission_overwrites,
            packet.name, packet.topic, packet.nsfw, packet.last_message_id, packet.parent_id,
            packet.last_pin_timestamp
        )
    )

    internal constructor(guild: Guild, packet: ChannelPacket) : this(
        guild,
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!,
            packet.name!!, packet.topic, packet.nsfw, packet.last_message_id, packet.parent_id,
            packet.last_pin_timestamp
        )
    )

    companion object {
        internal const val typeCode = 0

        fun find(id: Long) = Channel.find(id) as? GuildTextChannel
    }
}


class GuildVoiceChannel internal constructor(
    override val guild: Guild,
    packet: GuildVoiceChannelPacket
) : GuildChannel {
    override val id = packet.id
    override var name = packet.name
        private set
    var category: ChannelCategory? = packet.parent_id?.let { EntityCache.find(it) }
        private set
    override var position: Int = packet.position
        private set
    override val permissionOverwrites: Nothing get() = TODO("implement this")
    var bitrate: Int = packet.bitrate
        private set
    var userLimit: Int = packet.user_limit
        private set

    internal constructor(guild: Guild, packet: GuildChannelPacket) : this(
        guild,
        GuildVoiceChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position, packet.permission_overwrites, packet.name,
            packet.nsfw, packet.bitrate!!, packet.user_limit!!, packet.parent_id
        )
    )

    internal constructor(guild: Guild, packet: ChannelPacket) : this(
        guild,
        GuildVoiceChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!, packet.name!!,
            packet.nsfw, packet.bitrate!!, packet.user_limit!!, packet.parent_id
        )
    )

    companion object {
        internal const val typeCode = 2

        internal fun find(id: Long) = Channel.find(id) as? GuildVoiceChannel
    }
}


class ChannelCategory internal constructor(
    override val guild: Guild,
    packet: ChannelCategoryPacket
) : GuildChannel {
    override val id = packet.id
    override var name: String = packet.name
        private set
    override var position = packet.position
        private set
    override val permissionOverwrites: Nothing get() = TODO("not implemented")

    internal constructor(guild: Guild, packet: GuildChannelPacket) : this(
        guild,
        ChannelCategoryPacket(
            packet.id, packet.type, packet.guild_id, packet.name, packet.parent_id, packet.nsfw, packet.position,
            packet.permission_overwrites
        )
    )

    internal constructor(guild: Guild, packet: ChannelPacket) : this(
        guild,
        ChannelCategoryPacket(
            packet.id, packet.type, packet.guild_id, packet.name!!, packet.parent_id, packet.nsfw, packet.position!!,
            packet.permission_overwrites!!
        )
    )

    companion object {
        internal const val typeCode = 4

        fun find(id: Long) = Channel.find(id) as? ChannelCategory
    }
}
