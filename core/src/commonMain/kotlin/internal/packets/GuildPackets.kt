package com.serebit.strife.internal.packets

import kotlinx.serialization.Serializable

@Serializable
internal data class GuildCreatePacket(
    override val id: Long,
    var name: String,
    var icon: String?,
    var splash: String?,
    var owner: Boolean = false,
    var owner_id: Long,
    var permissions: Int = 0,
    var region: String,
    var afk_channel_id: Long?,
    var afk_timeout: Short,
    var embed_enabled: Boolean = false,
    var embed_channel_id: Long? = null,
    var verification_level: Byte,
    var default_message_notifications: Byte,
    var explicit_content_filter: Byte,
    var roles: List<RolePacket>,
    var emojis: List<EmotePacket>,
    var features: List<String>,
    var mfa_level: Byte,
    var application_id: Long?,
    var widget_enabled: Boolean = false,
    var widget_channel_id: Long? = null,
    var system_channel_id: Long?,
    var joined_at: String,
    var large: Boolean,
    var unavailable: Boolean,
    var member_count: Int,
    var voice_states: List<VoiceStatePacket>,
    var members: List<GuildMemberPacket>,
    val channels: MutableList<GenericGuildChannelPacket>,
    var presences: List<PresencePacket>
) : EntityPacket

/** https://discordapp.com/developers/docs/resources/guild#guild-object */
@Serializable
internal data class GuildUpdatePacket(
    override val id: Long,
    val name: String,
    val icon: String?,
    val splash: String?,
    val owner: Boolean = false,
    val owner_id: Long,
    val permissions: Int = 0,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Short,
    val embed_enabled: Boolean = false,
    val embed_channel_id: Long? = null,
    val verification_level: Byte,
    val default_message_notifications: Byte,
    val explicit_content_filter: Byte,
    val roles: List<RolePacket>,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Byte,
    val application_id: Long?,
    val widget_enabled: Boolean = false,
    val widget_channel_id: Long? = null,
    val system_channel_id: Long?
) : EntityPacket

@Serializable
internal data class UnavailableGuildPacket(
    override val id: Long,
    // if this is unset (which coerces to null), we've been kicked from this guild
    val unavailable: Boolean? = null
) : EntityPacket

@Serializable
internal data class GuildMemberPacket(
    val user: UserPacket,
    val nick: String? = null,
    val guild_id: Long? = null,
    val roles: List<Long>,
    val joined_at: String,
    val deaf: Boolean,
    val mute: Boolean
)

@Serializable
internal data class PartialMemberPacket(
    val user: UserPacket? = null,
    val nick: String? = null,
    val roles: List<Long> = emptyList(),
    val joined_at: String? = null,
    val deaf: Boolean = false,
    val mute: Boolean = false
)

@Serializable
internal data class PermissionOverwritePacket(
    val id: Long,
    val type: String,
    val allow: Int,
    val deny: Int
)

@Serializable
internal data class RolePacket(
    override val id: Long,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Short,
    val permissions: Int,
    val managed: Boolean,
    val mentionable: Boolean
) : EntityPacket

@Serializable
internal data class BanPacket(
    val user: UserPacket,
    val reason: String? = null
)
