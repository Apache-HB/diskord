package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildIntegration
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.internal.ISO
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable

@Serializable
internal data class GuildCreatePacket(
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
    val roles: List<GuildRolePacket>,
    val emojis: List<GuildEmojiPacket>,
    val features: List<String>,
    val mfa_level: Byte,
    val application_id: Long?,
    val widget_enabled: Boolean = false,
    val widget_channel_id: Long? = null,
    val system_channel_id: Long?,
    val joined_at: String? = null,
    val large: Boolean,
    val unavailable: Boolean = false,
    val member_count: Int,
    val voice_states: List<VoiceStatePacket>,
    val members: List<GuildMemberPacket>,
    val channels: List<GuildChannelPacket>,
    val presences: List<PresencePacket>
) : EntityPacket

@Serializable
internal data class PartialGuildPacket(
    override val id: Long,
    val name: String,
    val icon: String?,
    val splash: String? = null,
    val owner: Boolean? = null,
    val owner_id: Long? = null,
    val permissions: Int? = null,
    val region: String? = null,
    val afk_channel_id: Long? = null,
    val afk_timeout: Short? = null,
    val embed_enabled: Boolean? = null,
    val embed_channel_id: Long? = null,
    val verification_level: Byte? = null,
    val default_message_notifications: Byte? = null,
    val explicit_content_filter: Byte? = null,
    val roles: List<GuildRolePacket>? = null,
    val emojis: List<GuildEmojiPacket>? = null,
    val features: List<String>? = null,
    val mfa_level: Byte? = null,
    val application_id: Long? = null,
    val widget_enabled: Boolean? = null,
    val widget_channel_id: Long? = null,
    val system_channel_id: Long? = null,
    val joined_at: String? = null,
    val large: Boolean? = null,
    val unavailable: Boolean? = null,
    val member_count: Int? = null,
    val voice_states: List<VoiceStatePacket>? = null,
    val members: List<GuildMemberPacket>? = null,
    val channels: MutableList<GuildChannelPacket>? = null,
    val presences: List<PresencePacket>? = null
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
    val roles: List<GuildRolePacket>,
    val emojis: List<GuildEmojiPacket>,
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
internal data class GuildRolePacket(
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

@Serializable
internal data class PruneCountPacket(val pruned: Int?)

/** [See](https://discordapp.com/developers/docs/resources/voice#voice-region-object) */
@Serializable
internal data class VoiceRegionPacket(
    val id: String,
    val name: String,
    val vip: Boolean,
    val optimal: Boolean,
    val deprecated: Boolean,
    val custom: Boolean
)

@Serializable
internal data class GuildEmbedPacket(val enabled: Boolean, val channel_id: Long? = null)

/** [See](https://discordapp.com/developers/docs/resources/guild#get-guild-vanity-url) */
@Serializable
internal data class PartialInvitePacket(val code: String)

/** [See](https://discordapp.com/developers/docs/resources/guild#integration-object) */
@Serializable
internal data class GuildIntegrationPacket(
    val id: Long,
    val name: String,
    val type: String,
    val enabled: Boolean,
    val syncing: Boolean,
    val role_id: Long,
    val expire_behavior: Int,
    val expire_grace_period: Int,
    val user: UserPacket,
    val account: AccountPacket,
    val synced_at: String
) {
    @Serializable
    data class AccountPacket(val id: String, val name: String)
}

internal fun GuildIntegrationPacket.toIntegration(context: BotClient, guild: Guild, member: GuildMember) =
    GuildIntegration(
        context,
        id,
        guild,
        name,
        type,
        enabled,
        syncing,
        guild.getRole(role_id)!!,
        GuildIntegration.ExpireBehavior.values()[expire_behavior],
        expire_grace_period,
        member,
        GuildIntegration.Account(account.id, account.name),
        DateFormat.ISO.parse(synced_at)
    )
