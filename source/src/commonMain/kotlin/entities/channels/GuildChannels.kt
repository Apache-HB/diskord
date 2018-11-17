package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.PermissionOverride
import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket
import com.serebit.diskord.internal.packets.TextChannelPacket

interface GuildChannel : Channel {
    val position: Int
    val name: String
    val permissionOverrides: List<PermissionOverride?>

    companion object {
        internal fun from(packet: GuildChannelPacket): GuildChannel =
            EntityCache.findId(packet.id) ?: when (packet.type) {
                GuildTextChannel.typeCode -> GuildTextChannel(packet)
                GuildVoiceChannel.typeCode -> GuildVoiceChannel(packet)
                ChannelCategory.typeCode -> ChannelCategory(packet)
                else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of ${packet.type}.")
            }

        internal fun find(id: Long) = Channel.find(id) as? GuildChannel
    }
}


class GuildTextChannel internal constructor(private val packet: GuildTextChannelPacket) : TextChannel, GuildChannel {
    override val id = packet.id
    override val name = packet.name
    override val position = packet.position
    override val permissionOverrides by lazy { packet.permission_overwrites.mapNotNull { PermissionOverride.from(it) } }
    val category: ChannelCategory? by lazy { packet.parent_id?.let { EntityCache.findId<ChannelCategory>(it) } }
    val topic: String = packet.topic ?: ""
    val isNsfw: Boolean = packet.nsfw

    internal constructor(packet: TextChannelPacket) : this(
        GuildTextChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position!!, packet.permission_overwrites!!,
            packet.name!!, packet.topic!!, packet.nsfw, packet.last_message_id!!, packet.parent_id!!,
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
            packet.name!!, packet.topic!!, packet.nsfw, packet.last_message_id, packet.parent_id,
            packet.last_pin_timestamp
        )
    )

    companion object {
        internal const val typeCode = 0

        fun find(id: Long) = Channel.find(id) as? GuildTextChannel
    }
}


class GuildVoiceChannel internal constructor(
    packet: GuildVoiceChannelPacket
) : GuildChannel {
    override val id = packet.id
    override val name = packet.name
    override val position: Int = packet.position
    override val permissionOverrides by lazy { packet.permission_overwrites.mapNotNull { PermissionOverride.from(it) } }
    val category: ChannelCategory? by lazy { packet.parent_id?.let { EntityCache.findId<ChannelCategory>(it) } }
    val bitrate: Int = packet.bitrate
    val userLimit: Int = packet.user_limit

    internal constructor(packet: GuildChannelPacket) : this(
        GuildVoiceChannelPacket(
            packet.id, packet.type, packet.guild_id, packet.position, packet.permission_overwrites, packet.name,
            packet.nsfw, packet.bitrate!!, packet.user_limit!!, packet.parent_id
        )
    )

    internal constructor(packet: ChannelPacket) : this(
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


class ChannelCategory internal constructor(packet: ChannelCategoryPacket) : GuildChannel {
    override val id = packet.id
    override val name: String = packet.name
    override val position = packet.position
    override val permissionOverrides by lazy { packet.permission_overwrites.mapNotNull { PermissionOverride.from(it) } }

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

    companion object {
        internal const val typeCode = 4

        fun find(id: Long) = Channel.find(id) as? ChannelCategory
    }
}
