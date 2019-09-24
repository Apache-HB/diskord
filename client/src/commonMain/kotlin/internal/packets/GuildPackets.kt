package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildIntegration
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.internal.ISO
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

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

/** [See](https://discordapp.com/developers/docs/resources/audit-log#audit-logs-resource) */
@Serializable
internal data class AuditLogPacket(
    val webhooks: List<PartialWebhookPacket> = emptyList(),
    val users: List<BasicUserPacket> = emptyList(),
    val audit_log_entries: List<EntryPacket> = emptyList()
) {
    @Serializable
    data class EntryPacket(
        val target_id: Long? = null,
        val changes: List<ChangePacket>? = null,
        val user_id: Long,
        val id: Long,
        val action_type: Int,
        val options: OptionalEntryInfo? = null,
        val reason: String? = null
    )

    /**
     * [See](https://discordapp.com/developers/docs/resources/audit-log#audit-log-entry-object-optional-audit-entry-info)
     *
     * @property type "member" or "role"
     */
    @Serializable
    data class OptionalEntryInfo(
        val delete_member_days: Int? = null,
        val members_removed: Int? = null,
        val channel_id: Long? = null,
        val count: Int? = null,
        val id: Long? = null,
        val type: String? = null,
        val role_name: String? = null
    )

    @Serializable
    data class ChangePacket(
        val new_value: JsonElement? = null,
        val old_value: JsonElement? = null,
        val key: String
    ) {
        @Transient
        val keyType = Key[key]

        enum class Key(val serialName: String) {

            GuildName("name"), GuildIconHash("icon_hash"), GuildSplashHash("splash_hash"), GuildOwnerID("owner_id"),
            GuildRegion("region"), GuildAfkChannelID("afk_channel_id"), GuildAfkTimeout("afk_timeout"),
            GuildMfaLevel("mfa_level"), GuildVerificationLevel("verification_level"),
            GuildContentFilter("explicit_content_filter"),
            GuildDefaultMessageNotification("default_message_notifications"),
            GuildVanityUrl("vanity_url_code"), GuildRoleAdd("\$add"), GuildRoleRemove("\$remove"),
            GuildRolePermissions("permissions"), GuildRoleColor("color"), GuildRoleHoist("hoist"),
            GuildRoleMentionable("mentionable"), GuildRoleAllow("allow"), GuildRoleDeny("deny"),
            GuildPruneDays("prune_delete_days"), GuildWidgetEnabled("widget_enabled"),
            GuildWidgetChannelID("widget_channel_id"), ChannelPosition("position"), ChannelTopic("topic"),
            ChannelBitrate("bitrate"), ChannelPermissionOverwrites("permission_overwrites"), ChannelNsfw("nsfw"),
            ChannelApplicationID("application_id"), InviteCode("code"), InviteChannelID("channel_id"),
            InviterID("inviter_id"), InviteMaxUsers("max_uses"), InviteUses("uses"), InviteMaxAge("max_age"),
            InviteTemporary("temporary"), UserDeafenState("deaf"), UserMuteState("mute"), UserNickname("nick"),
            UserAvatarHash("avatar_hash"), GenericSnowflake("id"), Type("type");

            companion object {
                private val keys: Map<String, Key> by lazy {
                    mapOf(
                        GuildName.serialName to GuildName,
                        GuildIconHash.serialName to GuildIconHash,
                        GuildSplashHash.serialName to GuildSplashHash,
                        GuildOwnerID.serialName to GuildOwnerID,
                        GuildRegion.serialName to GuildRegion,
                        GuildAfkChannelID.serialName to GuildAfkChannelID,
                        GuildAfkTimeout.serialName to GuildAfkTimeout,
                        GuildMfaLevel.serialName to GuildMfaLevel,
                        GuildVerificationLevel.serialName to GuildVerificationLevel,
                        GuildContentFilter.serialName to GuildContentFilter,
                        GuildDefaultMessageNotification.serialName to GuildDefaultMessageNotification,
                        GuildVanityUrl.serialName to GuildVanityUrl,
                        GuildRoleAdd.serialName to GuildRoleAdd,
                        GuildRoleRemove.serialName to GuildRoleRemove,
                        GuildRolePermissions.serialName to GuildRolePermissions,
                        GuildRoleColor.serialName to GuildRoleColor,
                        GuildRoleHoist.serialName to GuildRoleHoist,
                        GuildRoleMentionable.serialName to GuildRoleMentionable,
                        GuildRoleAllow.serialName to GuildRoleAllow,
                        GuildRoleDeny.serialName to GuildRoleDeny,
                        GuildPruneDays.serialName to GuildPruneDays,
                        GuildWidgetEnabled.serialName to GuildWidgetEnabled,
                        GuildWidgetChannelID.serialName to GuildWidgetChannelID,
                        ChannelPosition.serialName to ChannelPosition,
                        ChannelTopic.serialName to ChannelTopic,
                        ChannelBitrate.serialName to ChannelBitrate,
                        ChannelPermissionOverwrites.serialName to ChannelPermissionOverwrites,
                        ChannelNsfw.serialName to ChannelNsfw,
                        ChannelApplicationID.serialName to ChannelApplicationID,
                        InviteCode.serialName to InviteCode,
                        InviteChannelID.serialName to InviteChannelID,
                        InviterID.serialName to InviterID,
                        InviteMaxUsers.serialName to InviteMaxUsers,
                        InviteUses.serialName to InviteUses,
                        InviteMaxAge.serialName to InviteMaxAge,
                        InviteTemporary.serialName to InviteTemporary,
                        UserDeafenState.serialName to UserDeafenState,
                        UserMuteState.serialName to UserMuteState,
                        UserNickname.serialName to UserNickname,
                        UserAvatarHash.serialName to UserAvatarHash,
                        GenericSnowflake.serialName to GenericSnowflake,
                        Type.serialName to Type
                    )
                }

                operator fun get(serialName: String) = keys[serialName]
            }

        }
    }
}
