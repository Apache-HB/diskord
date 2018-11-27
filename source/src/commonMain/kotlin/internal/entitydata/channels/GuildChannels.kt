package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.data.toOverrides
import com.serebit.diskord.internal.entitydata.MessageData
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket

internal class GuildTextChannelData(
    packet: GuildTextChannelPacket, override val context: Context
) : GuildChannelData, TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var guildId = packet.guild_id
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentId = packet.parent_id
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableListOf<MessageData>()
    override val lastMessage get() = messages.lastOrNull()
    var topic = packet.topic.orEmpty()
    var rateLimitPerUser = packet.rate_limit_per_user

    fun update(packet: GuildTextChannelPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        topic = packet.topic.orEmpty()
        isNsfw = packet.nsfw
        parentId = packet.parent_id
        rateLimitPerUser = packet.rate_limit_per_user
    }
}

internal class GuildVoiceChannelData(
    packet: GuildVoiceChannelPacket, override val context: Context
) : GuildChannelData {
    override val id = packet.id
    override val type = packet.type
    override var guildId = packet.guild_id
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentId = packet.parent_id
    var bitrate = packet.bitrate
    var userLimit = packet.user_limit

    fun update(packet: GuildVoiceChannelPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentId = packet.parent_id
        bitrate = packet.bitrate
        userLimit = packet.user_limit
    }
}

internal class ChannelCategoryData(packet: ChannelCategoryPacket, override val context: Context) : GuildChannelData {
    override val id = packet.id
    override val type = packet.type
    override var guildId = packet.guild_id
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentId = packet.parent_id

    fun update(packet: ChannelCategoryPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentId = packet.parent_id
    }
}
