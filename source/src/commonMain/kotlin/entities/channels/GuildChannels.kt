package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.data.PermissionOverride
import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetChannelCategory
import com.serebit.diskord.internal.network.endpoints.GetGuildTextChannel
import com.serebit.diskord.internal.network.endpoints.GetGuildVoiceChannel
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

interface GuildChannel : Channel {
    val position: Int
    val name: String
    val permissionOverrides: List<PermissionOverride>
    val guild: Guild

    companion object {
        internal fun from(packet: GuildChannelPacket): Channel = when (packet) {
            is GuildTextChannelPacket -> GuildTextChannel(packet.id)
            is GuildVoiceChannelPacket -> GuildVoiceChannel(packet.id)
            is ChannelCategoryPacket -> ChannelCategory(packet.id)
            else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of ${packet.type}.")
        }
    }
}

class GuildTextChannel internal constructor(override val id: Long) : TextChannel, GuildChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetGuildTextChannel(id))
            ?: throw EntityNotFoundException("Invalid guild text channel instantiated with ID $id.")
    override val name get() = packet.name
    override val guild get() = packet.guild
    override val position get() = packet.position
    override val permissionOverrides get() = packet.permissionOverrides
    override val lastMessage get() = packet.lastMessage
    override val lastPinTime get() = packet.lastPinTime
    val category get() = packet.parent
    val topic get() = packet.topicOrEmpty
    val isNsfw get() = packet.nsfw

    companion object {
        internal const val typeCode = 0
    }
}


class GuildVoiceChannel internal constructor(override val id: Long) : GuildChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetGuildVoiceChannel(id))
            ?: throw EntityNotFoundException("Invalid guild voice channel instantiated with ID $id.")
    override val name get() = packet.name
    override val position get() = packet.position
    override val permissionOverrides get() = packet.permissionOverrides
    override val guild get() = packet.guild
    val category get() = packet.parent
    val bitrate get() = packet.bitrate
    val userLimit get() = packet.user_limit

    companion object {
        internal const val typeCode = 2
    }
}


class ChannelCategory internal constructor(override val id: Long) : GuildChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetChannelCategory(id))
            ?: throw EntityNotFoundException("Invalid channel category instantiated with ID $id.")
    override val name get() = packet.name
    override val position get() = packet.position
    override val permissionOverrides get() = packet.permissionOverrides
    override val guild get() = packet.guild

    companion object {
        internal const val typeCode = 4
    }
}
