package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.toOverrides
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.packets.*
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse

internal interface ChannelData : EntityData {
    val type: Byte
}

internal interface TextChannelData : ChannelData {
    val lastMessage: MessageData?
    var lastPinTime: DateTimeTz?
    val messages: MutableMap<Long, MessageData>
}

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
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_FORMAT.parse(it) }
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

/** A private [TextChannelData] open only to the Bot and a single non-bot User. */
internal class DmChannelData(packet: DmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_FORMAT.parse(it) }
    override val messages = mutableMapOf<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var recipients = packet.recipients.map { recipient ->
        context.userCache[recipient.id] ?: recipient.toData(context)
            .also { context.userCache + (it.id to it) }
    }

    fun update(packet: DmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull {
            context.userCache[it.id]
        }
    }
}

internal class GroupDmChannelData(packet: GroupDmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_FORMAT.parse(it) }
    override val messages = mutableMapOf<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var recipients = packet.recipients.map { recipient ->
        context.userCache[recipient.id] ?: recipient.toData(context)
            .also { context.userCache + (it.id to it) }
    }
    var owner = context.userCache[packet.owner_id]!!
    var name = packet.name
    var iconHash = packet.icon

    fun update(packet: GroupDmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull {
            context.userCache[it.id]
        }
        owner = context.userCache[packet.owner_id]!!
        name = packet.name
        iconHash = packet.icon
    }
}

internal fun ChannelData.update(packet: ChannelPacket) = when (this) {
    is DmChannelData -> update(packet as DmChannelPacket)
    is GroupDmChannelData -> update(packet as GroupDmChannelPacket)
    is GuildChannelData -> update(packet as GuildChannelPacket)
    else -> throw IllegalStateException("Attempted to update an unknown ChannelData type.")
}

internal fun ChannelPacket.toData(context: Context) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GroupDmChannelPacket -> toGroupDmChannelData(context)
    is GuildChannelPacket -> toGuildChannelData(context.guildCache[guild_id!!]!!, context)
    else -> throw IllegalStateException("Attempted to convert an unknown ChannelPacket type to ChannelData.")
}

internal fun TextChannelPacket.toData(context: Context) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GroupDmChannelPacket -> toGroupDmChannelData(context)
    is GuildTextChannelPacket -> toGuildTextChannelData(context.guildCache[guild_id!!]!!, context)
    else -> throw IllegalStateException("Attempted to convert an unknown TextChannelPacket type to TextChannelData.")
}

internal fun GuildChannelPacket.toGuildChannelData(guildData: GuildData, context: Context) = when (this) {
    is GuildTextChannelPacket -> toGuildTextChannelData(guildData, context)
    is GuildVoiceChannelPacket -> toGuildVoiceChannelData(guildData, context)
    is GuildChannelCategoryPacket -> toGuildChannelCategoryData(guildData, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}

internal fun GuildChannelData.update(packet: GuildChannelPacket) = when (this) {
    is GuildTextChannelData -> update(packet as GuildTextChannelPacket)
    is GuildVoiceChannelData -> update(packet as GuildVoiceChannelPacket)
    is GuildChannelCategoryData -> update(packet as GuildChannelCategoryPacket)
    else -> throw IllegalStateException("Attempted to update an unknown GuildChannelData type.")
}

internal fun GuildTextChannelPacket.toGuildTextChannelData(guildData: GuildData, context: Context) =
    GuildTextChannelData(this, guildData, context)

internal fun GuildVoiceChannelPacket.toGuildVoiceChannelData(guildData: GuildData, context: Context) =
    GuildVoiceChannelData(this, guildData, context)

internal fun GuildChannelCategoryPacket.toGuildChannelCategoryData(guildData: GuildData, context: Context) =
    GuildChannelCategoryData(this, guildData, context)

internal fun DmChannelPacket.toDmChannelData(context: Context) = DmChannelData(this, context)
internal fun GroupDmChannelPacket.toGroupDmChannelData(context: Context) = GroupDmChannelData(this, context)
