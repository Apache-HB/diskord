package com.serebit.strife.internal.packets

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildIntegration
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.internal.ISO
import com.soywiz.klock.DateFormat
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl

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
    val webhooks: List<WebhookPacket> = emptyList(),
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
        val delete_member_days: Int,
        val members_removed: Int,
        val channel_id: Long,
        val count: Int,
        val id: Long,
        val type: String,
        val role_name: String
    )

    @Serializable(with = ChangePacket.Companion::class)
    data class ChangePacket(
        val new_value: Any? = null,
        val old_value: Any? = null,
        val key: Key<*>
    ) {
        @Serializer(forClass = ChangePacket::class)
        companion object : KSerializer<ChangePacket> {
            override val descriptor = object : SerialClassDescImpl("ChangePacket") {
                init {
                    addElement("new_value", true)
                    addElement("old_value", true)
                    addElement("key")
                }
            }

            override fun deserialize(decoder: Decoder): ChangePacket {
                // Initialize the decoder structure
                var dec = decoder.beginStructure(descriptor)
                // Get the key
                val key: Key<*> = Key[dec.decodeStringElement(descriptor, 2)]
                    ?: throw SerializationException("No key in ChangePacket at index 2")

                // Get the values as Any
                var newValue: Any? = null
                var oldValue: Any? = null
                loop@ while (true) {
                    when (val i = dec.decodeElementIndex(descriptor)) {
                        CompositeDecoder.READ_DONE -> break@loop
                        0 -> newValue = key.decode(dec, descriptor, i)
                        1 -> oldValue = key.decode(dec, descriptor, i)
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
                dec.endStructure(descriptor)
                return ChangePacket(newValue, oldValue, key)
            }
        }

        sealed class Key<T>(
            val serialName: String,
            val decode: (CompositeDecoder.(SerialClassDescImpl, Int) -> (T))
        ) {
            /** Guild name changed */
            object GuildName : Key<String>("name", { d, i -> decodeStringElement(d, i) })

            /**	guild	string	icon changed */
            object GuildIconHash : Key<String>("icon_hash", { d, i -> decodeStringElement(d, i) })

            /** guild	string	invite splash page artwork changed */
            object GuildSplashHash : Key<String>("splash_hash", { d, i -> decodeStringElement(d, i) })

            /**	guild	snowflake	owner changed */
            object GuildOwnerID : Key<Long>("owner_id", { d, i -> decodeLongElement(d, i) })

            /**	guild	string	region changed */
            object GuildRegion : Key<String>("region", { d, i -> decodeStringElement(d, i) })

            /** guild	snowflake	afk channel changed */
            object GuildAfkChannelID : Key<Long>("afk_channel_id", { d, i -> decodeLongElement(d, i) })

            /** guild	integer	afk timeout duration changed */
            object GuildAfkTimeout : Key<Int>("afk_timeout", { d, i -> decodeIntElement(d, i) })

            /** guild	integer	two-factor auth requirement changed */
            object GuildMfaLevel : Key<Int>("mfa_level", { d, i -> decodeIntElement(d, i) })

            /** guild	integer	required verification level changed */
            object GuildVerificationLevel : Key<Int>("verification_level", { d, i -> decodeIntElement(d, i) })

            /** guild	integer	change in whose messages are scanned and deleted for explicit content in the server */
            object GuildContentFilter : Key<Int>("explicit_content_filter", { d, i -> decodeIntElement(d, i) })

            /** guild	integer	default message notification level changed */
            object GuildDefaultMessageNotification :
                Key<Int>("default_message_notifications", { d, i -> decodeIntElement(d, i) })

            /** guild	string	guild invite vanity url changed */
            object GuildVanityUrl : Key<String>("vanity_url_code", { d, i -> decodeStringElement(d, i) })

            /** guild	array of role objects	new role added */
            object GuildRoleAdd : Key<List<GuildRolePacket>>(
                "\$add", { d, i -> decodeSerializableElement(d, i, GuildRolePacket.serializer().list) }
            )

            /** guild	array of role objects	role removed */
            object GuildRoleRemove : Key<List<GuildRolePacket>>(
                "\$remove", { d, i -> decodeSerializableElement(d, i, GuildRolePacket.serializer().list) }
            )

            /** role	integer	permissions for a role changed */
            object GuildRolePermissions : Key<Int>("permissions", { d, i -> decodeIntElement(d, i) })

            /** role	integer	role color changed */
            object GuildRoleColor : Key<Int>("color", { d, i -> decodeIntElement(d, i) })

            /** role	boolean	role is now displayed/no longer displayed separate from online users */
            object GuildRoleHoist : Key<Boolean>("hoist", { d, i -> decodeBooleanElement(d, i) })

            /** role	boolean	role is now mentionable/unmentionable */
            object GulidRoleMentionable : Key<Boolean>("mentionable", { d, i -> decodeBooleanElement(d, i) })

            /** role	integer	a permission on a text or voice channel was allowed for a role */
            object GuildRoleAllow : Key<Int>("allow", { d, i -> decodeIntElement(d, i) })

            //            /** role	integer	a permission on a text or voice channel was allowed for a role */
            object GuildRoleDeny : Key<Int>("deny", { d, i -> decodeIntElement(d, i) })

            /** guild	integer	change in number of days after which inactive and role-unassigned members are kicked*/
            object GuildPruneDays : Key<Int>("prune_delete_days", { d, i -> decodeIntElement(d, i) })

            /** guild	boolean	server widget enabled/disable */
            object GuildWidgetEnabled : Key<Boolean>("widget_enabled", { d, i -> decodeBooleanElement(d, i) })

            /** guild	snowflake	channel id of the server widget changed */
            object GuildWidgetChannelID : Key<Long>("widget_channel_id", { d, i -> decodeLongElement(d, i) })

            /** channel	integer	text or voice channel position changed */
            object ChannelPosition : Key<Int>("position", { d, i -> decodeIntElement(d, i) })

            /** channel	string	text channel topic changed */
            object ChannelTopic : Key<String>("topic", { d, i -> decodeStringElement(d, i) })

            /** channel	integer	voice channel bitrate changed */
            object ChannelBitrate : Key<Int>("bitrate", { d, i -> decodeIntElement(d, i) })

            /** channel	array of channel overwrite objects	permissions on a channel changed */
            object ChannelPermissionOverwrites : Key<List<PermissionOverwritePacket>>(
                "permission_overwrites",
                { d, i -> decodeSerializableElement(d, i, PermissionOverwritePacket.serializer().list) }
            )

            /** channel	boolean	channel nsfw restriction changed */
            object ChannelNsfw : Key<Boolean>("nsfw", { d, i -> decodeBooleanElement(d, i) })

            /** channel	snowflake	application id of the added or removed webhook or bot */
            object ChannelApplicationID : Key<Long>("application_id", { d, i -> decodeLongElement(d, i) })

            /** invite	string	invite code changed */
            object InviteCode : Key<String>("code", { d, i -> decodeStringElement(d, i) })

            /** invite	snowflake	channel for invite code changed */
            object InviteChannelID : Key<Long>("channel_id", { d, i -> decodeLongElement(d, i) })

            /** invite	snowflake	person who created invite code changed */
            object InviterID : Key<Long>("inviter_id", { d, i -> decodeLongElement(d, i) })

            /** invite	integer	change to max number of times invite code can be used */
            object InviteMaxUsers : Key<Int>("max_uses", { d, i -> decodeIntElement(d, i) })

            /** invite	integer	number of times invite code used changed */
            object InviteUses : Key<Int>("uses", { d, i -> decodeIntElement(d, i) })

            /** invite	integer	how long invite code lasts changed */
            object InviteMaxAge : Key<Int>("max_age", { d, i -> decodeIntElement(d, i) })

            /** invite	boolean	invite code is temporary/never expires */
            object InviteTemporary : Key<Boolean>("temporary", { d, i -> decodeBooleanElement(d, i) })

            /** user	boolean	user server deafened/undeafened */
            object UserDeafenState : Key<Boolean>("deaf", { d, i -> decodeBooleanElement(d, i) })

            /** user	boolean	user server muted/unmuted */
            object UserMuteState : Key<Boolean>("mute", { d, i -> decodeBooleanElement(d, i) })

            /** user	string	user nickname changed */
            object UserNickname : Key<String>("nick", { d, i -> decodeStringElement(d, i) })

            /** user	string	user avatar changed */
            object UserAvatarHash : Key<String>("avatar_hash", { d, i -> decodeStringElement(d, i) })

            /** any	snowflake	the id of the changed entity - sometimes used in conjunction with other keys */
            object GenericSnowflake : Key<Long>("id", { d, i -> decodeLongElement(d, i) })

            /** any	integer (channel type) or string	type of entity created */
            object Type : Key<String>("", { d, i -> decodeStringElement(d, i) })

            companion object {
                private val keys: Map<String, Key<*>> by lazy {
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
                        GulidRoleMentionable.serialName to GulidRoleMentionable,
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
