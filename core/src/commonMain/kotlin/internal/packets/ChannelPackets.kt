package com.serebit.strife.internal.packets

import com.serebit.strife.data.UnknownTypeCodeException
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.DmChannel
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildChannelCategory
import com.serebit.strife.entities.GuildNewsChannel
import com.serebit.strife.entities.GuildStoreChannel
import com.serebit.strife.entities.GuildTextChannel
import com.serebit.strife.entities.GuildVoiceChannel
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import kotlinx.serialization.Serializable

/** An [EntityPacket] with information about a [Channel][Channel]. */
internal interface ChannelPacket : EntityPacket {
    /** The [Channel Type](https://discordapp.com/developers/docs/resources/channel#channel-object-channel-types). */
    val type: Byte
}

/** A [ChannelPacket] for [TextChannels][TextChannel]. */
internal interface TextChannelPacket : ChannelPacket {
    /** The [id][Message.id] of the last [Message] sent in this [TextChannel]. */
    val last_message_id: Long?
    /** The timestamp of the last time a [Message] was pinned in this [TextChannel]. */
    val last_pin_timestamp: String?
}

/** A [ChannelPacket] for [GuildTextChannel] and [GuildVoiceChannel]. */
internal interface GuildChannelPacket : ChannelPacket {
    /** The [id][Guild.id] of the [ChannelPacket]. */
    var guild_id: Long?
    /** The positioning of the [Channel] in the [Guild]'s menu. */
    val position: Short
    val name: String
    val nsfw: Boolean
    val permission_overwrites: List<PermissionOverwritePacket>
    val parent_id: Long?
}

@Serializable
internal data class GuildTextChannelPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Byte? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
internal data class GuildNewsChannelPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    val topic: String? = null,
    override val nsfw: Boolean = false,
    override val last_message_id: Long? = null,
    override val parent_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
internal data class GuildStoreChannelPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val nsfw: Boolean = false,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long?,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    override val nsfw: Boolean = false,
    val bitrate: Int,
    val user_limit: Byte,
    override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
internal data class GuildChannelCategoryPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long? = null,
    override val name: String,
    override val parent_id: Long? = null,
    override val nsfw: Boolean = false,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>
) : GuildChannelPacket

@Serializable
internal data class DmChannelPacket(
    override val id: Long,
    override val type: Byte,
    val recipients: List<UserPacket>,
    override val last_message_id: Long? = null,
    override val last_pin_timestamp: String? = null
) : TextChannelPacket

@Serializable
internal data class GenericChannelPacket(
    val id: Long,
    val type: Byte,
    val guild_id: Long? = null,
    val position: Short? = null,
    val permission_overwrites: List<PermissionOverwritePacket> = emptyList(),
    val name: String? = null,
    val topic: String? = null,
    val nsfw: Boolean = false,
    val last_message_id: Long? = null,
    val bitrate: Int? = null,
    val user_limit: Byte? = null,
    val recipients: List<UserPacket> = emptyList(),
    val icon: String? = null,
    val owner_id: Long? = null,
    val application_id: Long? = null,
    val parent_id: Long? = null,
    val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Byte? = null
)

@Serializable
internal class GenericTextChannelPacket(
    val id: Long,
    val type: Byte,
    val guild_id: Long? = null,
    val position: Short? = null,
    val permission_overwrites: List<PermissionOverwritePacket> = emptyList(),
    val name: String? = null,
    val topic: String? = null,
    val nsfw: Boolean = false,
    val last_message_id: Long? = null,
    val recipients: List<UserPacket> = emptyList(),
    val icon: String? = null,
    val owner_id: Long? = null,
    val application_id: Long? = null,
    val parent_id: Long? = null,
    val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Byte? = null
)

@Serializable
internal data class GenericGuildChannelPacket(
    val id: Long,
    val type: Byte,
    val guild_id: Long? = null,
    val position: Short,
    val permission_overwrites: List<PermissionOverwritePacket> = emptyList(),
    val name: String? = null,
    val topic: String? = null,
    val nsfw: Boolean = false,
    val last_message_id: Long? = null,
    val bitrate: Int? = null,
    val user_limit: Byte? = null,
    val parent_id: Long? = null,
    val last_pin_timestamp: String? = null,
    val rate_limit_per_user: Byte? = null
)

internal fun GenericChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp, rate_limit_per_user!!
    )
    GuildNewsChannel.typeCode -> GuildNewsChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    GuildStoreChannel.typeCode -> GuildStoreChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, nsfw, parent_id
    )
    GuildVoiceChannel.typeCode -> GuildVoiceChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, nsfw, bitrate!!, user_limit!!, parent_id
    )
    GuildChannelCategory.typeCode -> GuildChannelCategoryPacket(
        id, type, guild_id, name!!, parent_id, nsfw, position!!, permission_overwrites
    )
    DmChannel.typeCode -> DmChannelPacket(id, type, recipients, last_message_id)
    else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of $type.")
}

internal fun GenericTextChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp, rate_limit_per_user!!
    )
    GuildNewsChannel.typeCode -> GuildNewsChannelPacket(
        id, type, guild_id, position!!, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    DmChannel.typeCode -> DmChannelPacket(id, type, recipients, last_message_id)
    else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of $type.")
}

internal fun GenericGuildChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp, rate_limit_per_user!!
    )
    GuildNewsChannel.typeCode -> GuildNewsChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    GuildStoreChannel.typeCode -> GuildStoreChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, nsfw, parent_id
    )
    GuildVoiceChannel.typeCode -> GuildVoiceChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, nsfw, bitrate!!, user_limit!!, parent_id
    )
    GuildChannelCategory.typeCode ->
        GuildChannelCategoryPacket(id, type, guild_id, name!!, parent_id, nsfw, position, permission_overwrites)
    else -> throw UnknownTypeCodeException("Received a guild channel with an unknown typecode of $type.")
}
