package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp

/**
 * https://discordapp.com/developers/docs/resources/guild#guild-object
 */
internal data class GuildPacket(
    val id: Long,
    val name: String,
    val roles: List<RolePacket>,
    val icon: String?,
    val splash: String?,
    val owner: Boolean?,
    val owner_id: Long,
    val permissions: BitSet?,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Int,
    val embed_enabled: Boolean?,
    val embed_channel_id: Long?,
    val verification_level: Int,
    val default_message_notifications: Int,
    val explicit_content_filter: Int,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Int,
    val application_id: Long?,
    val widget_enabled: Boolean?,
    val widget_channel_id: Long?,
    val system_channel_id: Long?,
    val joined_at: IsoTimestamp,
    val large: Boolean?,
    val unavailable: Boolean?,
    val member_count: Int?,
    val voice_states: List<VoiceStatePacket>,
    val members: List<MemberPacket>,
    val channels: List<GuildChannelPacket>,
    val presences: List<PresencePacket>
)

internal data class UnavailableGuildPacket(val unavailable: Boolean, val id: Long)

internal data class MemberPacket(
    val user: UserPacket,
    val nick: String?,
    val roles: List<Long>,
    val joined_at: IsoTimestamp,
    val deaf: Boolean,
    val mute: Boolean
)

internal data class PermissionOverwritePacket(
    val id: Long,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)

internal data class RolePacket(
    val id: Long,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
)
