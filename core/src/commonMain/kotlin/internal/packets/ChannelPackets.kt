package com.serebit.strife.internal.packets

import com.serebit.strife.data.UnknownTypeCodeException
import com.serebit.strife.entities.*
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

internal interface ChannelPacket : EntityPacket {
    val type: Byte
}

internal interface TextChannelPacket : ChannelPacket {
    val last_message_id: Long?
    val last_pin_timestamp: String?
}

internal interface GuildChannelPacket : ChannelPacket {
    var guild_id: Long?
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
    @Optional override var guild_id: Long? = null,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    @Optional val topic: String? = null,
    @Optional override val nsfw: Boolean = false,
    @Optional override val last_message_id: Long? = null,
    @Optional override val parent_id: Long? = null,
    @Optional override val last_pin_timestamp: String? = null,
    @Optional val rate_limit_per_user: Int? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    override val type: Byte,
    override var guild_id: Long?,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    @Optional override val nsfw: Boolean = false,
    val bitrate: Int,
    val user_limit: Byte,
    @Optional override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
internal data class GuildChannelCategoryPacket(
    override val id: Long,
    override val type: Byte,
    @Optional override var guild_id: Long? = null,
    override val name: String,
    @Optional override val parent_id: Long? = null,
    @Optional override val nsfw: Boolean = false,
    override val position: Short,
    override val permission_overwrites: List<PermissionOverwritePacket>
) : GuildChannelPacket

@Serializable
internal data class DmChannelPacket(
    override val id: Long,
    override val type: Byte,
    val recipients: List<UserPacket>,
    @Optional override val last_message_id: Long? = null,
    @Optional override val last_pin_timestamp: String? = null
) : TextChannelPacket

@Serializable
internal data class GroupDmChannelPacket(
    override val id: Long,
    override val type: Byte,
    val owner_id: Long,
    val name: String,
    val icon: String,
    val recipients: List<UserPacket>,
    @Optional override val last_message_id: Long? = null,
    @Optional override val last_pin_timestamp: String? = null
) : TextChannelPacket

@Serializable
internal data class GenericChannelPacket(
    val id: Long,
    val type: Byte,
    @Optional val guild_id: Long? = null,
    @Optional val position: Short? = null,
    @Optional val permission_overwrites: List<PermissionOverwritePacket>? = null,
    @Optional val name: String? = null,
    @Optional val topic: String? = null,
    @Optional val nsfw: Boolean = false,
    @Optional val last_message_id: Long? = null,
    @Optional val bitrate: Int? = null,
    @Optional val user_limit: Byte? = null,
    @Optional val recipients: List<UserPacket> = emptyList(),
    @Optional val icon: String? = null,
    @Optional val owner_id: Long? = null,
    @Optional val application_id: Long? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: String? = null,
    @Optional val rate_limit_per_user: Byte? = null
)

@Serializable
internal class GenericTextChannelPacket(
    val id: Long,
    val type: Byte,
    @Optional val guild_id: Long? = null,
    @Optional val position: Short? = null,
    @Optional val permission_overwrites: List<PermissionOverwritePacket>? = null,
    @Optional val name: String? = null,
    @Optional val topic: String? = null,
    @Optional val nsfw: Boolean = false,
    @Optional val last_message_id: Long? = null,
    @Optional val recipients: List<UserPacket> = emptyList(),
    @Optional val icon: String? = null,
    @Optional val owner_id: Long? = null,
    @Optional val application_id: Long? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: String? = null,
    @Optional val rate_limit_per_user: Byte? = null
)

@Serializable
internal data class GenericGuildChannelPacket(
    val id: Long,
    val type: Byte,
    @Optional val guild_id: Long? = null,
    val position: Short,
    @Optional val permission_overwrites: List<PermissionOverwritePacket> = emptyList(),
    @Optional val name: String? = null,
    @Optional val topic: String? = null,
    @Optional val nsfw: Boolean = false,
    @Optional val last_message_id: Long? = null,
    @Optional val bitrate: Int? = null,
    @Optional val user_limit: Byte? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: String? = null
)

internal fun GenericChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position!!, permission_overwrites!!, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    GuildVoiceChannel.typeCode -> GuildVoiceChannelPacket(
        id, type, guild_id, position!!, permission_overwrites!!, name!!, nsfw, bitrate!!, user_limit!!, parent_id
    )
    GuildChannelCategory.typeCode ->
        GuildChannelCategoryPacket(id, type, guild_id, name!!, parent_id, nsfw, position!!, permission_overwrites!!)
    DmChannel.typeCode -> DmChannelPacket(id, type, recipients, last_message_id)
    else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of $type.")
}

internal fun GenericTextChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position!!, permission_overwrites!!, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    DmChannel.typeCode -> DmChannelPacket(id, type, recipients, last_message_id)
    else -> throw UnknownTypeCodeException("Received a channel with an unknown typecode of $type.")
}

internal fun GenericGuildChannelPacket.toTypedPacket() = when (type) {
    GuildTextChannel.typeCode -> GuildTextChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, topic, nsfw, last_message_id, parent_id,
        last_pin_timestamp
    )
    GuildVoiceChannel.typeCode -> GuildVoiceChannelPacket(
        id, type, guild_id, position, permission_overwrites, name!!, nsfw, bitrate!!, user_limit!!, parent_id
    )
    GuildChannelCategory.typeCode ->
        GuildChannelCategoryPacket(id, type, guild_id, name!!, parent_id, nsfw, position, permission_overwrites)
    else -> throw UnknownTypeCodeException("Received a guild channel with an unknown typecode of $type.")
}
