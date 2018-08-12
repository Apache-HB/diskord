package com.serebit.diskord.packets

import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.Snowflake

internal data class ChannelPacket(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String?,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Boolean,
    val bitrate: Int?,
    val user_limit: Int?,
    val recipients: List<UserPacket>,
    val icon: String?,
    val owner_id: Snowflake?,
    val application_id: Snowflake?,
    val parent_id: Snowflake?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class GuildChannel(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val topic: String?,
    val nsfw: Boolean?,
    val last_message_id: Snowflake?,
    val bitrate: Int?,
    val user_limit: Int?,
    val parent_id: Snowflake?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class GuildTextChannelPacket(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val topic: String?,
    val nsfw: Boolean,
    val last_message_id: Snowflake?,
    val parent_id: Snowflake?,
    val last_pin_timestamp: IsoTimestamp?
)

internal data class GuildVoiceChannelPacket(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>,
    val name: String,
    val nsfw: Boolean,
    val bitrate: Int,
    val user_limit: Int,
    val parent_id: Snowflake?
)

internal data class DmChannelPacket(
    val id: Snowflake,
    val type: Int,
    val last_message_id: Snowflake?,
    val recipients: List<UserPacket>
)


internal data class GroupDmChannelPacket(
    val id: Snowflake,
    val type: Int,
    val owner_id: Snowflake,
    val name: String,
    val icon: String,
    val recipients: List<UserPacket>,
    val last_message_id: Snowflake?
)

internal data class ChannelCategoryPacket(
    val id: Snowflake,
    val type: Int,
    val guild_id: Snowflake?,
    val name: String,
    val parent_id: Snowflake?,
    val nsfw: Boolean,
    val position: Int,
    val permission_overwrites: List<PermissionOverwritePacket>
)
