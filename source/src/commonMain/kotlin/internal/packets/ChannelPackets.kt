package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
internal data class ChannelPacket(
    override val id: Long,
    val type: Int,
    @Optional val guild_id: Long? = null,
    @Optional val position: Int? = null,
    @Optional val permission_overwrites: List<PermissionOverwritePacket>? = null,
    @Optional val name: String? = null,
    @Optional val topic: String? = null,
    @Optional val nsfw: Boolean? = null,
    @Optional val last_message_id: Long? = null,
    @Optional val bitrate: Int? = null,
    @Optional val user_limit: Int? = null,
    @Optional val recipients: List<UserPacket>? = null,
    @Optional val icon: String? = null,
    @Optional val owner_id: Long? = null,
    @Optional val application_id: Long? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: IsoTimestamp? = null
) : EntityPacket {
    val asDmChannelPacket get() = DmChannelPacket(id, type, last_message_id, recipients!!)
}

@Serializable
internal data class TextChannelPacket(
    override val id: Long,
    val type: Int,
    @Optional val guild_id: Long? = null,
    @Optional val position: Int? = null,
    @Optional val permission_overwrites: List<PermissionOverwritePacket>? = null,
    @Optional val topic: String? = null,
    @Optional val nsfw: Boolean? = null,
    @Optional val last_message_id: Long? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: IsoTimestamp? = null,
    @Optional val recipients: List<UserPacket>? = null,
    @Optional val owner_id: Long? = null,
    @Optional val name: String? = null,
    @Optional val icon: String? = null
) : EntityPacket {
    val asDmChannelPacket get() = DmChannelPacket(id, type, last_message_id, recipients!!)
}

@Serializable
internal data class GuildChannelPacket(
    override val id: Long,
    val type: Int,
    @Optional val guild_id: Long? = null,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    @Optional val topic: String? = null,
    val nsfw: Boolean,
    @Optional val last_message_id: Long? = null,
    @Optional val bitrate: Int? = null,
    @Optional val user_limit: Int? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: IsoTimestamp? = null
) : EntityPacket

@Serializable
internal data class GuildTextChannelPacket(
    override val id: Long,
    val type: Int,
    @Optional val guild_id: Long? = null,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    @Optional val topic: String? = null,
    val nsfw: Boolean,
    @Optional val last_message_id: Long? = null,
    @Optional val parent_id: Long? = null,
    @Optional val last_pin_timestamp: IsoTimestamp? = null
) : EntityPacket

@Serializable
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val nsfw: Boolean,
    val bitrate: Int,
    val user_limit: Int,
    @Optional val parent_id: Long? = null
) : EntityPacket

@Serializable
internal data class DmChannelPacket(
    override val id: Long,
    val type: Int,
    @Optional val last_message_id: Long? = null,
    val recipients: List<UserPacket>
) : EntityPacket

@Serializable
internal data class GroupDmChannelPacket(
    override val id: Long,
    val type: Int,
    val owner_id: Long,
    val name: String,
    val icon: String,
    val recipients: List<UserPacket>,
    @Optional val last_message_id: Long? = null
) : EntityPacket

@Serializable
internal data class ChannelCategoryPacket(
    override val id: Long,
    val type: Int,
    @Optional val guild_id: Long? = null,
    val name: String,
    @Optional val parent_id: Long? = null,
    val nsfw: Boolean,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>
) : EntityPacket
