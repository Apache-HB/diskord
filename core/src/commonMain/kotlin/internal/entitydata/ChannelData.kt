package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.data.PermissionOverride
import com.serebit.strife.data.toOverrides
import com.serebit.strife.entities.*
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.WeakHashMap
import com.serebit.strife.internal.packets.*
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTimeTz
import com.soywiz.klock.parse

internal interface ChannelData<U : ChannelPacket, E : Channel> : EntityData<U, E> {
    val type: Byte
}

internal interface TextChannelData<U : TextChannelPacket, E : TextChannel> : ChannelData<U, E> {
    val lastMessage: MessageData?
    var lastPinTime: DateTimeTz?
    val messages: WeakHashMap<Long, MessageData>
}

internal interface GuildChannelData<U : GuildChannelPacket, E : GuildChannel> : ChannelData<U, E> {
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
    override val context: BotClient
) : GuildChannelData<GuildTextChannelPacket, GuildTextChannel>,
    TextChannelData<GuildTextChannelPacket, GuildTextChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildTextChannel(this) }
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_WITH_MS.parse(it) }
    override val messages = WeakHashMap<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var topic = packet.topic.orEmpty()
    var rateLimitPerUser = packet.rate_limit_per_user

    override fun update(packet: GuildTextChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        topic = packet.topic.orEmpty()
        isNsfw = packet.nsfw
        parentID = packet.parent_id
        rateLimitPerUser = packet.rate_limit_per_user
    }
}

internal class GuildNewsChannelData(
    packet: GuildNewsChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildNewsChannelPacket, GuildNewsChannel>,
    TextChannelData<GuildNewsChannelPacket, GuildNewsChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildNewsChannel(this) }
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_WITH_MS.parse(it) }
    override val messages = WeakHashMap<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var topic = packet.topic.orEmpty()

    override fun update(packet: GuildNewsChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        topic = packet.topic.orEmpty()
        isNsfw = packet.nsfw
        parentID = packet.parent_id
    }
}

internal class GuildStoreChannelData(
    packet: GuildStoreChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildStoreChannelPacket, GuildStoreChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildStoreChannel(this) }
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id

    override fun update(packet: GuildStoreChannelPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentID = packet.parent_id
    }
}

internal class GuildVoiceChannelData(
    packet: GuildVoiceChannelPacket,
    override val guild: GuildData,
    override val context: BotClient
) : GuildChannelData<GuildVoiceChannelPacket, GuildVoiceChannel> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildVoiceChannel(this) }
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id
    var bitrate = packet.bitrate
    var userLimit = packet.user_limit

    override fun update(packet: GuildVoiceChannelPacket) {
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
    override val context: BotClient
) : GuildChannelData<GuildChannelCategoryPacket, GuildChannelCategory> {
    override val id = packet.id
    override val lazyEntity by lazy { GuildChannelCategory(this) }
    override val type = packet.type
    override var position = packet.position
    override var permissionOverrides = packet.permission_overwrites.toOverrides()
    override var name = packet.name
    override var isNsfw = packet.nsfw
    override var parentID = packet.parent_id

    override fun update(packet: GuildChannelCategoryPacket) {
        position = packet.position
        permissionOverrides = packet.permission_overwrites.toOverrides()
        name = packet.name
        isNsfw = packet.nsfw
        parentID = packet.parent_id
    }
}

/** A private [TextChannelData] open only to the Bot and a single non-bot User. */
internal class DmChannelData(packet: DmChannelPacket, override val context: BotClient) :
    TextChannelData<DmChannelPacket, DmChannel> {

    override val id = packet.id
    override val lazyEntity by lazy { DmChannel(this) }
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.let { DateFormat.ISO_WITH_MS.parse(it) }
    override val messages = WeakHashMap<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var recipients = packet.recipients.map { context.cache.pullUserData(it) }

    override fun update(packet: DmChannelPacket) {
        recipients = packet.recipients.map { context.cache.pullUserData(it) }
    }
}

internal fun ChannelPacket.toData(context: BotClient) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GuildChannelPacket -> toGuildChannelData(
        context.cache.getGuildData(guild_id!!)!!,
        context
    ) // TODO guild_id nulls
    else -> throw IllegalStateException("Attempted to convert an unknown ChannelPacket type to ChannelData.")
}

internal fun TextChannelPacket.toData(context: BotClient) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GuildTextChannelPacket -> toGuildTextChannelData(context.cache.getGuildData(guild_id!!)!!, context)
    else -> throw IllegalStateException("Attempted to convert an unknown TextChannelPacket type to TextChannelData.")
}

internal fun GuildChannelPacket.toGuildChannelData(guildData: GuildData, context: BotClient) = when (this) {
    is GuildTextChannelPacket -> toGuildTextChannelData(guildData, context)
    is GuildNewsChannelPacket -> toGuildNewsChannelData(guildData, context)
    is GuildStoreChannelPacket -> toGuildStoreChannelData(guildData, context)
    is GuildVoiceChannelPacket -> toGuildVoiceChannelData(guildData, context)
    is GuildChannelCategoryPacket -> toGuildChannelCategoryData(guildData, context)
    else -> throw IllegalStateException("Attempted to convert an unknown GuildChannelPacket type to GuildChannelData.")
}

internal fun GuildTextChannelPacket.toGuildTextChannelData(guildData: GuildData, context: BotClient) =
    GuildTextChannelData(this, guildData, context)

internal fun GuildNewsChannelPacket.toGuildNewsChannelData(guildData: GuildData, context: BotClient) =
    GuildNewsChannelData(this, guildData, context)

internal fun GuildStoreChannelPacket.toGuildStoreChannelData(guildData: GuildData, context: BotClient) =
    GuildStoreChannelData(this, guildData, context)

internal fun GuildVoiceChannelPacket.toGuildVoiceChannelData(guildData: GuildData, context: BotClient) =
    GuildVoiceChannelData(this, guildData, context)

internal fun GuildChannelCategoryPacket.toGuildChannelCategoryData(guildData: GuildData, context: BotClient) =
    GuildChannelCategoryData(this, guildData, context)

internal fun DmChannelPacket.toDmChannelData(context: BotClient) = DmChannelData(this, context)
