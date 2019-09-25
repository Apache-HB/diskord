package com.serebit.strife.data

import com.serebit.strife.data.AuditLog.AuditLogEntryimport com.serebit.strife.data.AuditLog.AuditLogEntry.EntryChangeimport com.serebit.strife.data.AuditLog.AuditLogEntry.EntryInfo.OverwriteInfo.EntryOverwriteTypeimport com.serebit.strife.entities.*import com.serebit.strife.internal.entitydata.GuildDataimport com.serebit.strife.internal.network.Routeimport com.serebit.strife.internal.packets.AuditLogPacketimport com.serebit.strife.internal.packets.AuditLogPacket.ChangePacketimport com.serebit.strife.internal.packets.AuditLogPacket.EntryPacketimport com.serebit.strife.internal.packets.PermissionOverwritePacketimport kotlinx.coroutines.flow.Flowimport kotlinx.coroutines.flow.flowimport kotlinx.serialization.UnstableDefaultimport kotlinx.serialization.json.Json

/**
 * The [AuditLog] is the ledger of a [Guild]; it contains any administrative action performed in a list of [entries].
 *
 * @property guild The [Guild] this [AuditLog] is from.
 * @property entries The list of [Entries][AuditLog.AuditLogEntry] in this [AuditLog], this list holds only the most recent 100
 * entries, use [getHistory][AuditLog.getHistory] function to get a [Flow] of all entries.
 * @property webhookIDs A list of [Webhook.id]s found in the [AuditLog].
 * @property members A list of [GuildMember]s found in the [AuditLog].
 * @property userIDs A list of [User IDs][User.id] found in the [AuditLog].
 */
data class AuditLog internal constructor(
    private val guildData: GuildData,
    val entries: List<AuditLogEntry> = emptyList(),
    val webhookIDs: List<Long> = emptyList(),
    val userIDs: Set<Long> = emptySet(),
    val members: Set<GuildMember> = emptySet()
) {

    /**
     * Whenever an admin action is performed on the API, an entry is added to the respective guild's audit log.
     *
     * @property id The unique ID of this entry.
     * @property targetID [unique ID][Entity.id] of the affected [Entity] (webhook, user, role, etc.).
     * @property type The type of [AuditLogEvent] this entry represents.
     * @property userID The [user ID][User.id] of the [User] which performed the action.
     * @property member The [GuildMember] associated with the [userID]. `null` if the member could not be found
     * (e.g. the user is no longer in the [guild]).
     * @property changes A list of [changes][EntryChange] which this entry is logging.
     * @property extraInfo Additional information regarding this entry.
     * @property reason The reasoning for this entry (e.g. The reason for a member being kicked).
     */
    data class AuditLogEntry internal constructor(
        val id: Long,
        val targetID: Long?,
        val type: AuditLogEvent,
        val userID: Long,
        val member: GuildMember?,
        val changes: List<EntryChange<*>>,
        val extraInfo: EntryInfo?,
        val reason: String?
    ) {

        /** Additional information regarding an [AuditLogEntry]. */
        sealed class EntryInfo {

            /**
             * Additional Information about an [AuditLogEvent.MEMBER_PRUNE].
             *
             * @property inactiveMemberDays number of days after which inactive members were kicked.
             * @property pruneResult number of members removed by a prune.
             */
            class PruneInfo(val inactiveMemberDays: Int? = null, val pruneResult: Int? = null) : EntryInfo()

            /**
             * @property channelID channel in which messages were deleted.
             * @property deleteCount number of deleted messages in [channelID].
             */
            class MessageDeleteInfo(val channelID: Long? = null, val deleteCount: Int? = null) : EntryInfo()

            /**
             * Additional Information about an [AuditLogEvent.CHANNEL_OVERWRITE_CREATE],
             * [AuditLogEvent.CHANNEL_OVERWRITE_UPDATE], or [AuditLogEvent.CHANNEL_OVERWRITE_DELETE].
             *
             * @property overwrittenID id of an overwritten entity. Applicable for
             * @property overwrittenType The type of overwritten entity. Either [EntryOverwriteType.MEMBER] or
             * [EntryOverwriteType.ROLE].
             * @property roleName The name of the [GuildRole] if [overwrittenType] is [EntryOverwriteType.ROLE].
             */
            class OverwriteInfo(
                val overwrittenID: Long? = null,
                val overwrittenType: EntryOverwriteType? = null,
                val roleName: String? = null
            ) : EntryInfo() {
                /** Used for [OverwriteInfo.EntryOverwriteType] */
                enum class EntryOverwriteType {
                    /** Used when an [OverwriteInfo] is about a [GuildMember] overwrite. */
                    MEMBER,
                    /** Used when an [OverwriteInfo] is about a [GuildRole] overwrite. */
                    ROLE
                }
            }

            /** Used when the [AuditLogEntry.extraInfo] is of an known type. */
            object UnknownInfoType : EntryInfo()
        }

        /**
         * An [EntryChange] contains information about a changed value of type [T].
         *
         * @param T The type of the changed information.
         * @property oldValue The previous value of the changed information.
         * @property newValue The new value of the changed information.
         */
        sealed class EntryChange<T>(val oldValue: T? = null, val newValue: T? = null) {

            /** Represents a [Guild.name] being changed. */
            class GuildName internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild.icon] being changed. */
            class GuildIconHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild.splashImage] being changed. */
            class GuildSplashHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild.getOwner] being changed. */
            class GuildOwnerID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Guild.region] being changed. */
            class GuildRegion internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild.afkChannel] being changed. */
            class GuildAfkChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Guild.afkTimeout] being changed. */
            class GuildAfkTimeout internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [Guild.mfaLevel] being changed. */
            class GuildMfaLevel internal constructor(old: MfaLevel?, new: MfaLevel?) : EntryChange<MfaLevel>(old, new)

            /** Represents a [Guild.verificationLevel] being changed. */
            class GuildVerificationLevel internal constructor(old: VerificationLevel?, new: VerificationLevel?) :
                EntryChange<VerificationLevel>(old, new)

            /** Represents a [Guild.explicitContentFilter] being changed. */
            class GuildExplicitContentFilterLevel internal constructor(
                old: ExplicitContentFilterLevel?,
                new: ExplicitContentFilterLevel?
            ) :
                EntryChange<ExplicitContentFilterLevel>(old, new)

            /** Represents a [Guild.defaultMessageNotifications] being changed. */
            class GuildMessageNotificationLevel internal constructor(
                old: MessageNotificationLevel?,
                new: MessageNotificationLevel?
            ) :
                EntryChange<MessageNotificationLevel>(old, new)

            /** Represents a [Guild.getVanityUrl] being changed. */
            class GuildVanityUrl internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [GuildRole] being added. */
            class GuildRoleAdd internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** Represents a [GuildRole] being removed. */
            class GuildRoleRemove internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** Represents a [GuildRole.permissions] being changed. */
            class GuildRolePermissions internal constructor(old: Set<Permission>?, new: Set<Permission>?) :
                EntryChange<Set<Permission>>(old, new)

            /** Represents a [GuildRole.color] being changed. */
            class GuildRoleColor internal constructor(old: Color?, new: Color?) : EntryChange<Color>(old, new)

            /** Represents a [GuildRole.isHoisted] being changed. */
            class GuildRoleHoist internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildRole.isMentionable] being changed. */
            class GuildRoleMentionable internal constructor(old: Boolean?, new: Boolean?) :
                EntryChange<Boolean>(old, new)

            /** Represents a [Permission] being allowed for a [GuildRole]. */
            class GuildRoleAllow internal constructor(old: Permission?, new: Permission?) :
                EntryChange<Permission>(old, new)

            /** Represents a [Permission] being denied for a [GuildRole]. */
            class GuildRoleDeny internal constructor(old: Permission?, new: Permission?) :
                EntryChange<Permission>(old, new)

            /** Represents the number of days after which inactive and role-unassigned [GuildMember]s are kicked. */
            class GuildPruneDays internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildEmbed] being dis/enabled. */
            class GuildWidgetEnabled internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildEmbed.channel] being changed. */
            class GuildWidgetChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [GuildChannel.position] being changed. */
            class ChannelPosition internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildMessageChannel.topic] being changed. */
            class ChannelTopic internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [GuildVoiceChannel.bitrate] being changed. */
            class ChannelBitrate internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildChannel.permissionOverrides] being changed. */
            class ChannelPermissionOverwrites internal constructor(
                old: List<PermissionOverride>?, new: List<PermissionOverride>?
            ) : EntryChange<List<PermissionOverride>>(old, new)

            /** Represents a [GuildMessageChannel.isNsfw] being changed. */
            class ChannelNsfw internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildTextChannel] application being changed. */
            class ChannelApplicationID internal constructor(old: Long?, new: Long?) :
                EntryChange<Long>(old, new)

            /** Represents a [Invite.code] being changed. */
            class InviteCode internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Invite.channel] being changed. */
            class InviteChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Invite.inviter] being changed. */
            class InviterID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Invite.useLimit] being changed. */
            class InviteMaxUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [Invite.useCount] being changed. */
            class InviteUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [Invite.activeTimeRange] being changed. */
            class InviteMaxAge internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [Invite.temporary] being changed. */
            class InviteTemporary internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember.isDeafened] being changed. */
            class UserDeafenState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember.isMuted] being changed. */
            class UserMuteState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember.nickname] being changed. */
            class UserNickname internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [User.avatar] being changed. */
            class UserAvatarHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /**
             * Represents any snowflake ID of a changed entity -
             * sometimes used in conjunction with other [EntryChange]s.
             */
            class GenericSnowflake internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** any	integer (channel type) or string type of entity created */
            class Type internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)
        }
    }

    val guild: Guild get() = guildData.lazyEntity

    /**
     * Returns a [Flow] of [AuditLogEntry]. The flow can be filtered with these optional parameters:
     *
     * [limit]: maximum number of [AuditLogEntry] to retrive.
     * [userID]: filter for entries made by the [user ID][User.id]
     * [eventType]: filter for entries of the [AuditLogEvent]
     * [beforeEntryID]: filter for entries before the given entry
     *
     * This functions operates by making repeated requests to the Discord API, there is a limit to the number of failed
     * requests allowed before the flow will close which can be set by [maxFail] (defaults to 10).
     */
    suspend fun getHistory(
        limit: Int? = null,
        userID: Long? = null,
        eventType: AuditLogEvent? = null,
        beforeEntryID: Long? = null,
        maxFail: Int? = null,
        collector: (suspend (AuditLogEntry) -> Unit)? = null
    ): Flow<AuditLogEntry> = flow {

        require(limit?.let { it > 1 } ?: true) { "Limit must be greater than 0" }

        val apiLimit = 100
        var before = beforeEntryID
        var retrieveCount = 0
        val failLimit = maxFail ?: 10
        var failCount = 0

        loop@ while (limit?.let { retrieveCount < it } != false && failCount < failLimit) {
            val batch: Int = when {
                limit == null -> apiLimit
                (limit - retrieveCount) in 1..apiLimit -> limit - retrieveCount
                else -> apiLimit
            }
            val entries = guild.context.requester.sendRequest(
                Route.GetGuildAuditLog(guild.id, userID, eventType, before, batch)
            ).value
                ?.audit_log_entries
                ?.map { it.toAuditLogEntry(guildData) }

            when (entries?.size) {
                null -> failCount++
                0 -> break@loop
                else -> {
                    retrieveCount += batch
                    before = entries.last().id
                    entries.forEach { emit(it) }
                }
            }
        }
    }.also { f -> collector?.run { f.collect { invoke(it) } } }

}

/**
 * An [AuditLogEvent] is the type of action performed in an [AuditLogEntry].
 *
 * @property id The API ID of the [AuditLogEvent].
 */
@Suppress("KDocMissingDocumentation")
enum class AuditLogEvent(val id: Int) {
    GUILD_UPDATE(1),
    CHANNEL_CREATE(10),
    CHANNEL_UPDATE(11),
    CHANNEL_DELETE(12),
    CHANNEL_OVERWRITE_CREATE(13),
    CHANNEL_OVERWRITE_UPDATE(14),
    CHANNEL_OVERWRITE_DELETE(15),
    MEMBER_KICK(20),
    MEMBER_PRUNE(21),
    MEMBER_BAN_ADD(22),
    MEMBER_BAN_REMOVE(23),
    MEMBER_UPDATE(24),
    MEMBER_ROLE_UPDATE(25),
    ROLE_CREATE(30),
    ROLE_UPDATE(31),
    ROLE_DELETE(32),
    INVITE_CREATE(40),
    INVITE_UPDATE(41),
    INVITE_DELETE(42),
    WEBHOOK_CREATE(50),
    WEBHOOK_UPDATE(51),
    WEBHOOK_DELETE(52),
    EMOJI_CREATE(60),
    EMOJI_UPDATE(61),
    EMOJI_DELETE(62),
    MESSAGE_DELETE(72);

    companion object {
        private val map by lazy { values().associateBy { it.id } }
        /** Returns the [AuditLogEvent] with the given [id]. */
        operator fun get(id: Int) = map[id]
    }
}

internal fun AuditLogPacket.toAuditLog(guildData: GuildData): AuditLog = AuditLog(
    guildData,
    audit_log_entries.map { it.toAuditLogEntry(guildData) },
    webhooks.map { it.id },
    users.map { it.id }.toSet(),
    users.mapNotNull { guildData.getMemberData(it.id)?.lazyMember }.toSet()
)

internal fun EntryPacket.toAuditLogEntry(guildData: GuildData): AuditLogEntry = AuditLogEntry(
    id,
    target_id,
    AuditLogEvent[action_type]!!,
    user_id,
    guildData.getMemberData(user_id)?.lazyMember,
    changes?.map { it.toAuditLogEntryChange() } ?: emptyList(),
    options?.toEntryInfo(),
    reason
)

internal fun AuditLogPacket.OptionalEntryInfo.toEntryInfo() = when {
    delete_member_days != null -> PruneInfo(delete_member_days, members_removed)
    channel_id != null -> MessageDeleteInfo(channel_id, count)
    id != null -> OverwriteInfo(id, type?.toUpperCase()?.let { EntryOverwriteType.valueOf(it) }, role_name)
    else -> UnknownInfoType
}


@UseExperimental(UnstableDefault::class)
internal fun ChangePacket.toAuditLogEntryChange() = keyType?.invoke(this) ?: error("Audit Change Key type not found")

@UseExperimental(UnstableDefault::class)
private val changeMapping = mapOf<ChangePacket.Key, (ChangePacket) -> EntryChange<*>>(
<<<<<<< HEAD
    ChangePacket.Key.GuildName to { it ->
        EntryChange.GuildName(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GuildIconHash to { it ->
        EntryChange.GuildIconHash(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GuildSplashHash to { it ->
        EntryChange.GuildSplashHash(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GuildOwnerID to { it ->
        EntryChange.GuildOwnerID(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.GuildRegion to { it ->
        EntryChange.GuildRegion(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GuildAfkChannelID to { it ->
        EntryChange.GuildAfkChannelID(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.GuildAfkTimeout to { it ->
        EntryChange.GuildAfkTimeout(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
    ChangePacket.Key.GuildMfaLevel to { it ->
        EntryChange.GuildMfaLevel(
            it.old_value?.primitive?.intOrNull?.let { i -> MfaLevel.values()[i] },
            it.new_value?.primitive?.intOrNull?.let { i -> MfaLevel.values()[i] }
        )
    },
    ChangePacket.Key.GuildVerificationLevel to { it ->
        EntryChange.GuildVerificationLevel(
            it.old_value?.primitive?.intOrNull?.let { i -> VerificationLevel.values()[i] },
            it.new_value?.primitive?.intOrNull?.let { i -> VerificationLevel.values()[i] }
        )
    },
    ChangePacket.Key.GuildContentFilter to { it ->
        EntryChange.GuildExplicitContentFilterLevel(
            it.old_value?.primitive?.intOrNull?.let { i -> ExplicitContentFilterLevel.values()[i] },
            it.new_value?.primitive?.intOrNull?.let { i -> ExplicitContentFilterLevel.values()[i] }
        )
    },
    ChangePacket.Key.GuildDefaultMessageNotification to { it ->
        EntryChange.GuildMessageNotificationLevel(
            it.old_value?.primitive?.intOrNull?.let { i -> MessageNotificationLevel.values()[i] },
            it.new_value?.primitive?.intOrNull?.let { i -> MessageNotificationLevel.values()[i] }
        )
    },
    ChangePacket.Key.GuildVanityUrl to { it ->
        EntryChange.GuildVanityUrl(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GuildRoleAdd to { it ->
        EntryChange.GuildRoleAdd(
            it.old_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.primitive?.longOrNull },
            it.new_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.primitive?.longOrNull }
        )
    },
    ChangePacket.Key.GuildRoleRemove to { it ->
        EntryChange.GuildRoleRemove(
            it.old_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.primitive?.longOrNull },
            it.new_value?.jsonArray?.mapNotNull { rp -> rp.jsonObject["id"]?.primitive?.longOrNull }
        )
    },
    ChangePacket.Key.GuildRolePermissions to { it ->
        EntryChange.GuildRolePermissions(
            it.old_value?.primitive?.intOrNull?.toPermissions(),
            it.new_value?.primitive?.intOrNull?.toPermissions()
        )
    },
    ChangePacket.Key.GuildRoleColor to { it ->
        EntryChange.GuildRoleColor(
            it.old_value?.primitive?.intOrNull?.let { rgb -> Color(rgb) },
            it.new_value?.primitive?.intOrNull?.let { rgb -> Color(rgb) }
        )
    },
    ChangePacket.Key.GuildRoleHoist to { it ->
        EntryChange.GuildRoleHoist(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.GuildRoleMentionable to { it ->
        EntryChange.GuildRoleMentionable(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.GuildRoleAllow to { it ->
        EntryChange.GuildRoleAllow(
            it.old_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull(),
            it.new_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull()
        )
    },
    ChangePacket.Key.GuildRoleDeny to { it ->
        EntryChange.GuildRoleDeny(
            it.old_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull(),
            it.new_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull()
        )
    },
    ChangePacket.Key.GuildPruneDays to { it ->
        EntryChange.GuildPruneDays(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
    ChangePacket.Key.GuildWidgetEnabled to { it ->
        EntryChange.GuildWidgetEnabled(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.GuildWidgetChannelID to { it ->
        EntryChange.GuildWidgetChannelID(
            it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull
        )
    },
    ChangePacket.Key.ChannelPosition to { it ->
        EntryChange.ChannelPosition(
            it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull
        )
    },
    ChangePacket.Key.ChannelTopic to { it ->
        EntryChange.ChannelTopic(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.ChannelBitrate to { it ->
        EntryChange.ChannelBitrate(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
=======

>>>>>>> e01ac42... started moving conversion mapping into enums
    ChangePacket.Key.ChannelPermissionOverwrites to { it ->
        EntryChange.ChannelPermissionOverwrites(
            it.old_value?.jsonArray?.mapNotNull { po ->
                Json.parse(PermissionOverwritePacket.serializer(), po.toString()).toOverride()
            },
            it.new_value?.jsonArray?.mapNotNull { po ->
                Json.parse(PermissionOverwritePacket.serializer(), po.toString()).toOverride()
            }
        )
    },
    ChangePacket.Key.ChannelNsfw to { it ->
        EntryChange.ChannelNsfw(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.ChannelApplicationID to { it ->
        EntryChange.ChannelApplicationID(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.InviteCode to { it ->
        EntryChange.InviteCode(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.InviteChannelID to { it ->
        EntryChange.InviteChannelID(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.InviterID to { it ->
        EntryChange.InviterID(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.InviteMaxUses to { it ->
        EntryChange.InviteMaxUses(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
    ChangePacket.Key.InviteUses to { it ->
        EntryChange.InviteUses(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
    ChangePacket.Key.InviteMaxAge to { it ->
        EntryChange.InviteMaxAge(it.old_value?.primitive?.intOrNull, it.new_value?.primitive?.intOrNull)
    },
    ChangePacket.Key.InviteTemporary to { it ->
        EntryChange.InviteTemporary(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.UserDeafenState to { it ->
        EntryChange.UserDeafenState(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.UserMuteState to { it ->
        EntryChange.UserMuteState(it.old_value?.primitive?.booleanOrNull, it.new_value?.primitive?.booleanOrNull)
    },
    ChangePacket.Key.UserNickname to { it ->
        EntryChange.UserNickname(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.UserAvatarHash to { it ->
        EntryChange.UserAvatarHash(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    },
    ChangePacket.Key.GenericSnowflake to { it ->
        EntryChange.GenericSnowflake(it.old_value?.primitive?.longOrNull, it.new_value?.primitive?.longOrNull)
    },
    ChangePacket.Key.Type to { it ->
        EntryChange.Type(it.old_value?.primitive?.contentOrNull, it.new_value?.primitive?.contentOrNull)
    }
)
