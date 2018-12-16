package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.data.toOverrides
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.packets.GuildChannelCategoryPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.GuildVoiceChannelPacket
import com.serebit.strife.time.toDateTime

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
    override val messages = mutableMapOf<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
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

internal class GuildChannelCategoryData(
    packet: GuildChannelCategoryPacket,
    override val context: Context
) : GuildChannelData {
    override val id = packet.id
    override val type = packet.type
    override var guildId = packet.guild_id
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentId = packet.parent_id

    fun update(packet: GuildChannelCategoryPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentId = packet.parent_id
    }
}

internal fun GuildTextChannelPacket.toGuildTextChannelData(context: Context) = GuildTextChannelData(this, context)

internal fun GuildVoiceChannelPacket.toGuildVoiceChannelData(context: Context) = GuildVoiceChannelData(this, context)

internal fun GuildChannelCategoryPacket.toGuildChannelCategoryData(context: Context) =
    GuildChannelCategoryData(this, context)
