package com.serebit.strife.internal.packets

import com.serebit.strife.BitSet
import com.serebit.strife.IsoTimestamp
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
    @Optional var permissions: BitSet = 0,
    var region: String,
    var afk_channel_id: Long?,
    var afk_timeout: Int,
    @Optional var embed_enabled: Boolean = false,
    @Optional var embed_channel_id: Long? = null,
    var verification_level: Int,
    var default_message_notifications: Int,
    var explicit_content_filter: Int,
    var roles: List<RolePacket>,
    var emojis: List<EmotePacket>,
    var features: List<String>,
    var mfa_level: Int,
    var application_id: Long?,
    @Optional var widget_enabled: Boolean = false,
    @Optional var widget_channel_id: Long? = null,
    var system_channel_id: Long?,
    var joined_at: IsoTimestamp,
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
    @Optional val permissions: BitSet = 0,
    val region: String,
    val afk_channel_id: Long?,
    val afk_timeout: Int,
    @Optional val embed_enabled: Boolean = false,
    @Optional val embed_channel_id: Long? = null,
    val verification_level: Int,
    val default_message_notifications: Int,
    val explicit_content_filter: Int,
    val roles: List<RolePacket>,
    val emojis: List<EmotePacket>,
    val features: List<String>,
    val mfa_level: Int,
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
    val joined_at: IsoTimestamp,
    val deaf: Boolean,
    val mute: Boolean
)

@Serializable
internal data class PartialMemberPacket(
    @Optional val user: UserPacket? = null,
    @Optional val nick: String? = null,
    @Optional val roles: List<Long> = emptyList(),
    @Optional val joined_at: IsoTimestamp? = null,
    @Optional val deaf: Boolean = false,
    @Optional val mute: Boolean = false
)

@Serializable
internal data class PermissionOverwritePacket(
    val id: Long,
    val type: String,
    val allow: BitSet,
    val deny: BitSet
)

@Serializable
internal data class RolePacket(
    override val id: Long,
    val name: String,
    val color: Int,
    val hoist: Boolean,
    val position: Int,
    val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
) : EntityPacket
