package com.serebit.diskord.internal.packets

import com.serebit.diskord.IsoTimestamp

internal data class ChannelPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int?,
    val permission_overwrites: List<PermissionOverwritePacket>?,
    val name: String?,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Long?,
    val bitrate: Int?,
    val user_limit: Int?,
    val recipients: List<UserPacket>?,
    val icon: String?,
    val owner_id: Long?,
    val application_id: Long?,
    val parent_id: Long?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class TextChannelPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int?,
    val permission_overwrites: List<PermissionOverwritePacket>?,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Long?,
    val parent_id: Long?,
    val last_pin_timestamp: IsoTimestamp?,
    val recipients: List<UserPacket>?,
    val owner_id: Long?,
    val name: String?,
    val icon: String?
)

internal data class GuildChannelPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Long?,
    val bitrate: Int?,
    val user_limit: Int?,
    val parent_id: Long?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class GuildTextChannelPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Long?,
    val parent_id: Long?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class GuildVoiceChannelPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val nsfw: Boolean?,
    val bitrate: Int,
    val user_limit: Int,
    val parent_id: Long?
)

internal data class DmChannelPacket(
    val id: Long,
    val type: Int,
    val last_message_id: Long?,
    val recipients: List<UserPacket>
)

internal data class GroupDmChannelPacket(
    val id: Long,
    val type: Int,
    val owner_id: Long,
    val name: String,
    val icon: String,
    val recipients: List<UserPacket>,
    val last_message_id: Long?
)

internal data class ChannelCategoryPacket(
    val id: Long,
    val type: Int,
    val guild_id: Long?,
    val name: String,
    val parent_id: Long?,
    val nsfw: Boolean?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>
)
