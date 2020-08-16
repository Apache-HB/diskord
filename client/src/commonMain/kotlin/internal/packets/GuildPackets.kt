package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryChange
import com.serebit.strife.data.toOverride
import com.serebit.strife.data.toPermissions
import com.serebit.strife.entities.*
import com.serebit.strife.internal.ISO
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.*

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
internal data class PruneCountPacket(val pruned: Int? = null)

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
            GuildName("name") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildName(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GuildIconHash("icon_hash") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildIconHash(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GuildSplashHash("splash_hash") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildSplashHash(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GuildOwnerID("owner_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildOwnerID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            GuildRegion("region") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRegion(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GuildAfkChannelID("afk_channel_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildAfkChannelID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            GuildAfkTimeout("afk_timeout") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildAfkTimeout(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            GuildMfaLevel("mfa_level") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildMfaLevel(
                    packet.old_value?.jsonPrimitive?.intOrNull?.let { MfaLevel.values()[it] },
                    packet.new_value?.jsonPrimitive?.intOrNull?.let { MfaLevel.values()[it] }
                )
            },
            GuildVerificationLevel("verification_level") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildVerificationLevel(
                    packet.old_value?.jsonPrimitive?.intOrNull?.let { VerificationLevel.values()[it] },
                    packet.new_value?.jsonPrimitive?.intOrNull?.let { VerificationLevel.values()[it] }
                )
            },
            GuildContentFilter("explicit_content_filter") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildExplicitContentFilterLevel(
                    packet.old_value?.jsonPrimitive?.intOrNull?.let { ExplicitContentFilterLevel.values()[it] },
                    packet.new_value?.jsonPrimitive?.intOrNull?.let { ExplicitContentFilterLevel.values()[it] }
                )
            },
            GuildDefaultMessageNotification("default_message_notifications") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildMessageNotificationLevel(
                    packet.old_value?.jsonPrimitive?.intOrNull?.let { MessageNotificationLevel.values()[it] },
                    packet.new_value?.jsonPrimitive?.intOrNull?.let { MessageNotificationLevel.values()[it] }
                )
            },
            GuildVanityUrl("vanity_url_code") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildVanityUrl(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GuildRoleAdd("\$add") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleAdd(
                    packet.old_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.jsonPrimitive?.longOrNull },
                    packet.new_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.jsonPrimitive?.longOrNull }
                )
            },
            GuildRoleRemove("\$remove") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleRemove(
                    packet.old_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.jsonPrimitive?.longOrNull },
                    packet.new_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.jsonPrimitive?.longOrNull }
                )
            },
            GuildRolePermissions("permissions") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRolePermissions(
                    packet.old_value?.jsonPrimitive?.intOrNull?.toPermissions(),
                    packet.new_value?.jsonPrimitive?.intOrNull?.toPermissions()
                )
            },
            GuildRoleColor("color") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleColor(
                    packet.old_value?.jsonPrimitive?.intOrNull?.let { rgb -> com.serebit.strife.data.Color(rgb) },
                    packet.new_value?.jsonPrimitive?.intOrNull?.let { rgb -> com.serebit.strife.data.Color(rgb) }
                )
            },
            GuildRoleHoist("hoist") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleHoist(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            GuildRoleMentionable("mentionable") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleMentionable(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            GuildRoleAllow("allow") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleAllow(
                    packet.old_value?.jsonPrimitive?.intOrNull?.toPermissions()?.firstOrNull(),
                    packet.new_value?.jsonPrimitive?.intOrNull?.toPermissions()?.firstOrNull()
                )
            },
            GuildRoleDeny("deny") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildRoleDeny(
                    packet.old_value?.jsonPrimitive?.intOrNull?.toPermissions()?.firstOrNull(),
                    packet.new_value?.jsonPrimitive?.intOrNull?.toPermissions()?.firstOrNull()
                )
            },
            GuildPruneDays("prune_delete_days") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildPruneDays(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            GuildWidgetEnabled("widget_enabled") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildWidgetEnabled(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            GuildWidgetChannelID("widget_channel_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GuildWidgetChannelID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            ChannelPosition("position") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelPosition(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            ChannelTopic("topic") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelTopic(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            ChannelBitrate("bitrate") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelBitrate(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            ChannelPermissionOverwrites("permission_overwrites") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelPermissionOverwrites(
                    packet.old_value?.jsonArray?.toOverride(), packet.new_value?.toOverride()
                )

                private fun JsonElement.toOverride() = jsonArray.mapNotNull { po ->
                    Json.decodeFromJsonElement(PermissionOverwritePacket.serializer(), po).toOverride()
                }
            },
            ChannelNsfw("nsfw") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelNsfw(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            ChannelApplicationID("application_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.ChannelApplicationID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            InviteCode("code") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteCode(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            InviteChannelID("channel_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteChannelID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            InviterID("inviter_id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviterID(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            InviteMaxUses("max_uses") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteMaxUses(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            InviteUses("uses") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteUses(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            InviteMaxAge("max_age") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteMaxAge(
                    packet.old_value?.jsonPrimitive?.intOrNull, packet.new_value?.jsonPrimitive?.intOrNull
                )
            },
            InviteTemporary("temporary") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.InviteTemporary(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            UserDeafenState("deaf") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.UserDeafenState(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            UserMuteState("mute") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.UserMuteState(
                    packet.old_value?.jsonPrimitive?.booleanOrNull, packet.new_value?.jsonPrimitive?.booleanOrNull
                )
            },
            UserNickname("nick") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.UserNickname(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            UserAvatarHash("avatar_hash") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.UserAvatarHash(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            },
            GenericSnowflake("id") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.GenericSnowflake(
                    packet.old_value?.jsonPrimitive?.longOrNull, packet.new_value?.jsonPrimitive?.longOrNull
                )
            },
            Type("type") {
                override fun toEntryChange(packet: ChangePacket) = EntryChange.Type(
                    packet.old_value?.jsonPrimitive?.contentOrNull, packet.new_value?.jsonPrimitive?.contentOrNull
                )
            };

            abstract fun toEntryChange(packet: ChangePacket): EntryChange<*>

            companion object {
                private val keys: Map<String, Key> by lazy { values().map { it.serialName to it }.toMap() }

                /** Get a key by it's serialized name */
                operator fun get(serialName: String) = keys[serialName]
            }

        }
    }
}
