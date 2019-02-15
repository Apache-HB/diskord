package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.toOverrides
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.packets.GuildChannelCategoryPacket
import com.serebit.strife.internal.packets.GuildChannelPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.GuildVoiceChannelPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse

internal interface GuildChannelData : ChannelData {
    val guild: GuildData
    var position: Short
    var name: String
    var isNsfw: Boolean
    var permissionOverrides: List<PermissionOverride>
    var parentID: Long?
}

internal class GuildTextChannelData(
    packet: GuildTextChannelPacket,
    override val guild: GuildData,
    override val context: Context
) : GuildChannelData, TextChannelData {

    override val id = packet.id
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id
    override var lastPinTime = packet.last_pin_timestamp?.let {
        DateFormat.ISO_FORMAT.parse(it)
    }
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
        parentID = packet.parent_id
        rateLimitPerUser = packet.rate_limit_per_user
    }
}

internal class GuildVoiceChannelData(
    packet: GuildVoiceChannelPacket,
    override val guild: GuildData,
    override val context: Context
) : GuildChannelData {
    override val id = packet.id
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id
    var bitrate = packet.bitrate
    var userLimit = packet.user_limit

    fun update(packet: GuildVoiceChannelPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentID = packet.parent_id
        bitrate = packet.bitrate
        userLimit = packet.user_limit
    }
}

internal class GuildChannelCategoryData(
    packet: GuildChannelCategoryPacket,
    override val guild: GuildData,
    override val context: Context
) : GuildChannelData {
    override val id = packet.id
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id

    fun update(packet: GuildChannelCategoryPacket) = apply {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentID = packet.parent_id
    }
}

internal fun GuildTextChannelPacket.toGuildTextChannelData(guildData: GuildData, context: Context) =
    GuildTextChannelData(this, guildData, context)

internal fun GuildVoiceChannelPacket.toGuildVoiceChannelData(guildData: GuildData, context: Context) =
    GuildVoiceChannelData(this, guildData, context)

internal fun GuildChannelCategoryPacket.toGuildChannelCategoryData(guildData: GuildData, context: Context) =
    GuildChannelCategoryData(this, guildData, context)

internal fun GuildChannelData.update(packet: GuildChannelPacket) = when (this) {
    is GuildTextChannelData -> update(packet as GuildTextChannelPacket)
    is GuildVoiceChannelData -> update(packet as GuildVoiceChannelPacket)
    is GuildChannelCategoryData -> update(packet as GuildChannelCategoryPacket)
    else -> throw IllegalStateException("Attempted to update an unknown GuildChannelData type.")
}

internal fun GuildChannelPacket.toGuildChannelData(guildData: GuildData, context: Context) = when (this) {
    is GuildTextChannelPacket -> GuildTextChannelData(this, guildData, context)
    is GuildVoiceChannelPacket -> GuildVoiceChannelData(this, guildData, context)
    is GuildChannelCategoryPacket -> GuildChannelCategoryData(this, guildData, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}
