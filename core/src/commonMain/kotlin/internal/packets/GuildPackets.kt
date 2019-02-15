package com.serebit.strife.internal.packets

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
internal data class GuildCreatePacket(
    override val id: Long,
    var name: String,
    var icon: String?,
    var splash: String?,
    @Optional var owner: Boolean = false,
    var owner_id: Long,
    @Optional var permissions: Int = 0,
    var region: String,
    var afk_channel_id: Long?,
    var afk_timeout: Short,
    @Optional var embed_enabled: Boolean = false,
    @Optional var embed_channel_id: Long? = null,
    var verification_level: Byte,
    var default_message_notifications: Byte,
    var explicit_content_filter: Byte,
    var roles: List<RolePacket>,
    var emojis: List<EmotePacket>,
    var features: List<String>,
    var mfa_level: Byte,
    var application_id: Long?,
    @Optional var widget_enabled: Boolean = false,
    @Optional var widget_channel_id: Long? = null,
    var system_channel_id: Long?,
    var joined_at: String,
    var large: Boolean,
    var unavailable: Boolean,
    var member_count: Int,
    var voice_states: List<VoiceStatePacket>,
    var members: List<MemberPacket>,
    val channels: MutableList<GenericGuildChannelPacket>,
    var presences: List<PresencePacket>
) : EntityPacket

/**
 * https://discordapp.com/developers/docs/resources/guild#guild-object
 */
@Serializable
internal data class GuildUpdatePacket(
    override val id: Long,
    val name: String,
    val icon: String?,
    val splash: String?,
    @Optional val owner: Boolean = false,
    val owner_id: Long,
    @Optional val permissions: Int = 0,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Short,
    @Optional val embed_enabled: Boolean = false,
    @Optional val embed_channel_id: Long? = null,
    val verification_level: Byte,
    val default_message_notifications: Byte,
    val explicit_content_filter: Byte,
    val roles: List<RolePacket>,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Byte,
    val application_id: Long?,
    @Optional val widget_enabled: Boolean = false,
    @Optional val widget_channel_id: Long? = null,
    val system_channel_id: Long?
) : EntityPacket

@Serializable
internal data class UnavailableGuildPacket(
    override val id: Long,
    // if this is unset (which coerces to null), we've been kicked from this guild
    @Optional val unavailable: Boolean? = null
) : EntityPacket

@Serializable
internal data class MemberPacket(
    val user: UserPacket,
    @Optional val nick: String? = null,
    val roles: List<Long>,
    val joined_at: String,
    val deaf: Boolean,
    val mute: Boolean
)

@Serializable
internal data class PartialMemberPacket(
    @Optional val user: UserPacket? = null,
    @Optional val nick: String? = null,
    @Optional val roles: List<Long> = emptyList(),
    @Optional val joined_at: String? = null,
    @Optional val deaf: Boolean = false,
    @Optional val mute: Boolean = false
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
