package com.serebit.diskord.internal.packets

import com.serebit.diskord.Context
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.UnknownTypeCodeException
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.data.toOverrides
import com.serebit.diskord.entities.channels.ChannelCategory
import com.serebit.diskord.entities.channels.DmChannel
import com.serebit.diskord.entities.channels.GroupDmChannel
import com.serebit.diskord.entities.channels.GuildTextChannel
import com.serebit.diskord.entities.channels.GuildVoiceChannel
import com.serebit.diskord.internal.entitydata.channels.ChannelCategoryData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildVoiceChannelData
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

internal interface ChannelPacket : EntityPacket {
    val type: Int
}

internal interface TextChannelPacket : ChannelPacket {
    val last_message_id: Long?
    val last_pin_timestamp: IsoTimestamp?
}

internal interface GuildChannelPacket : ChannelPacket {
    var guild_id: Long?
    val position: Int
    val name: String
    val nsfw: Boolean
    val permission_overwrites: List<PermissionOverwritePacket>
    val parent_id: Long?
}

@Serializable
internal data class GuildTextChannelPacket(
    override val id: Long,
    override val type: Int,
    @Optional override var guild_id: Long? = null,
    override val position: Int,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    @Optional val topic: String? = null,
    @Optional override val nsfw: Boolean = false,
    @Optional override val last_message_id: Long? = null,
    @Optional override val parent_id: Long? = null,
    @Optional override val last_pin_timestamp: IsoTimestamp? = null,
    @Optional val rate_limit_per_user: Int? = null
) : TextChannelPacket, GuildChannelPacket

@Serializable
internal data class GuildVoiceChannelPacket(
    override val id: Long,
    override val type: Int,
    override var guild_id: Long?,
    override val position: Int,
    override val permission_overwrites: List<PermissionOverwritePacket>,
    override val name: String,
    @Optional override val nsfw: Boolean = false,
    val bitrate: Int,
    val user_limit: Int,
    @Optional override val parent_id: Long? = null
) : GuildChannelPacket

@Serializable
internal data class ChannelCategoryPacket(
    override val id: Long,
    override val type: Int,
    @Optional override var guild_id: Long? = null,
    override val name: String,
    @Optional override val parent_id: Long? = null,
    @Optional override val nsfw: Boolean = false,
    override val position: Int,
    override val permission_overwrites: List<PermissionOverwritePacket>
) : GuildChannelPacket

@Serializable
internal data class DmChannelPacket(
    override val id: Long,
    override val type: Int,
    val recipients: List<UserPacket>,
    @Optional override val last_message_id: Long? = null,
    @Optional override val last_pin_timestamp: IsoTimestamp? = null
) : TextChannelPacket

@Serializable
internal data class GroupDmChannelPacket(
    override val id: Long,
    override val type: Int,
    val owner_id: Long,
    val name: String,
    val icon: String,
    val recipients: List<UserPacket>,
    @Optional override val last_message_id: Long? = null,
    @Optional override val last_pin_timestamp: IsoTimestamp? = null
) : TextChannelPacket
