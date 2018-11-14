package com.serebit.diskord.internal.packets

import com.serebit.diskord.BitSet
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.data.Color
import com.serebit.diskord.data.toPermissions
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal data class GuildCreatePacket(
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
    val system_channel_id: Long?,
    val joined_at: IsoTimestamp,
    val large: Boolean,
    val unavailable: Boolean,
    val member_count: Int,
    val voice_states: List<VoiceStatePacket>,
    val members: List<MemberPacket>,
    val channels: List<GuildChannelPacket>,
    val presences: List<PresencePacket>
) : EntityPacket

/**
 * https://discordapp.com/developers/docs/resources/guild#guild-object
 */
@Serializable
internal data class GuildPacket(
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
    private val color: Int,
    val hoist: Boolean,
    val position: Int,
    private val permissions: BitSet,
    val managed: Boolean,
    val mentionable: Boolean
) : EntityPacket {
    @Transient
    val colorObj by lazy { Color(color) }
    @Transient
    val permissionsList by lazy { permissions.toPermissions() }
}
