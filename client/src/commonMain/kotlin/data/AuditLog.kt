package com.serebit.strife.data

import com.serebit.strife.data.AuditLog.AuditLogEntryimport com.serebit.strife.data.AuditLog.AuditLogEntry.EntryChangeimport com.serebit.strife.data.AuditLog.AuditLogEntry.EntryInfo.*import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryInfo.OverwriteInfo.EntryOverwriteTypeimport com.serebit.strife.entities.*import com.serebit.strife.internal.entitydata.GuildDataimport com.serebit.strife.internal.network.Routeimport com.serebit.strife.internal.packets.AuditLogPacketimport com.serebit.strife.internal.packets.AuditLogPacket.ChangePacketimport com.serebit.strife.internal.packets.AuditLogPacket.EntryPacketimport com.serebit.strife.internal.packets.PermissionOverwritePacketimport kotlinx.coroutines.flow.Flowimport kotlinx.coroutines.flow.collectimport kotlinx.coroutines.flow.flowimport kotlinx.serialization.UnstableDefaultimport kotlinx.serialization.json.Json

/**
 * The [AuditLog] is the ledger of a [Guild]; it contains any administrative action performed in a list of [entries].
 *
 * @property guild The [Guild] this [AuditLog] is from.
 * @property entries The list of [Entires][AuditLog.AuditLogEntry] in this [AuditLog], this list holds only the most recent 100
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
     * @property targetID [ID][Entity.id] of the affected [Entity]. (webhook, user, role, etc.)
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
                /** Used for [EntryInfo.overwrittenType] */
                enum class EntryOverwriteType {
                    MEMBER, ROLE
                }
            }

            object UnknownInfoType : EntryInfo()
        }

        sealed class EntryChange<T>(val oldValue: T? = null, val newValue: T? = null) {

            /** Guild name changed */
            class GuildName internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /**	guild	string	icon changed */
            class GuildIconHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	string	invite splash page artwork changed */
            class GuildSplashHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /**	guild	snowflake	owner changed */
            class GuildOwnerID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /**	guild	string	region changed */
            class GuildRegion internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	snowflake	afk channel changed */
            class GuildAfkChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** guild	integer	afk timeout duration changed */
            class GuildAfkTimeout internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** guild	integer	two-factor auth requirement changed */
            class GuildMfaLevel internal constructor(old: MfaLevel?, new: MfaLevel?) : EntryChange<MfaLevel>(old, new)

            /** guild	integer	required verification level changed */
            class GuildVerificationLevel internal constructor(old: VerificationLevel?, new: VerificationLevel?) :
                EntryChange<VerificationLevel>(old, new)

            /** guild	integer	change in whose messages are scanned and deleted for explicit content in the server */
            class GuildExplicitContentFilterLevel internal constructor(
                old: ExplicitContentFilterLevel?,
                new: ExplicitContentFilterLevel?
            ) :
                EntryChange<ExplicitContentFilterLevel>(old, new)

            /** guild	integer	default message notification level changed */
            class GuildMessageNotificationLevel internal constructor(
                old: MessageNotificationLevel?,
                new: MessageNotificationLevel?
            ) :
                EntryChange<MessageNotificationLevel>(old, new)

            /** guild	string	guild invite vanity url changed */
            class GuildVanityUrl internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	array of role classs	new role added */
            class GuildRoleAdd internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** guild	array of role classs	role removed */
            class GuildRoleRemove internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** role	integer	permissions for a role changed */
            class GuildRolePermissions internal constructor(old: Set<Permission>?, new: Set<Permission>?) :
                EntryChange<Set<Permission>>(old, new)

            /** role	integer	role color changed */
            class GuildRoleColor internal constructor(old: Color?, new: Color?) : EntryChange<Color>(old, new)

            /** role	boolean	role is now displayed/no longer displayed separate from online users */
            class GuildRoleHoist internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** role	boolean	role is now mentionable/unmentionable */
            class GuildRoleMentionable internal constructor(old: Boolean?, new: Boolean?) :
                EntryChange<Boolean>(old, new)

            /** role	integer	a permission on a text or voice channel was allowed for a role */
            class GuildRoleAllow internal constructor(old: Permission?, new: Permission?) :
                EntryChange<Permission>(old, new)

            /** role	integer	a permission on a text or voice channel was allowed for a role */
            class GuildRoleDeny internal constructor(old: Permission?, new: Permission?) :
                EntryChange<Permission>(old, new)

            /** guild	integer	change in number of days after which inactive and role-unassigned members are kicked*/
            class GuildPruneDays internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** guild	boolean	server widget enabled/disable */
            class GuildWidgetEnabled internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** guild	snowflake	channel id of the server widget changed */
            class GuildWidgetChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** channel	integer	text or voice channel position changed */
            class ChannelPosition internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** channel	string	text channel topic changed */
            class ChannelTopic internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** channel	integer	voice channel bitrate changed */
            class ChannelBitrate internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** channel	array of channel overwrite classs	permissions on a channel changed */
            class ChannelPermissionOverwrites internal constructor(
                old: List<PermissionOverride>?, new: List<PermissionOverride>?
            ) : EntryChange<List<PermissionOverride>>(old, new)

            /** channel	boolean	channel nsfw restriction changed */
            class ChannelNsfw internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** channel	snowflake	application id of the added or removed webhook or bot */
            class ChannelApplicationID internal constructor(old: Long?, new: Long?) :
                EntryChange<Long>(old, new)

            /** invite	string	invite code changed */
            class InviteCode internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** invite	snowflake	channel for invite code changed */
            class InviteChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** invite	snowflake	person who created invite code changed */
            class InviterID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** invite	integer	change to max number of times invite code can be used */
            class InviteMaxUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	integer	number of times invite code used changed */
            class InviteUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	integer	how long invite code lasts changed. See [Invite.activeTimeRange]. */
            class InviteMaxAge internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	boolean	invite code is temporary/never expires */
            class InviteTemporary internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	boolean	user server deafened/undeafened */
            class UserDeafenState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	boolean	user server muted/unmuted */
            class UserMuteState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	string	user nickname changed */
            class UserNickname internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** user	string	user avatar changed */
            class UserAvatarHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** any	snowflake	the id of the changed entity - sometimes used in conjunction with other keys */
            class GenericSnowflake internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** any	integer (channel type) or string	type of entity created */
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
     */
    suspend fun getHistory(
        limit: Int? = null,
        userID: Long? = null,
        eventType: AuditLogEvent? = null,
        beforeEntryID: Long? = null,
        collector: (suspend (AuditLogEntry) -> Unit)? = null
    ): Flow<AuditLogEntry> = flow {

        require(limit?.let { it > 1 } ?: true) { "Limit must be greater than 0" }

        val apiLimit = 100
        var before = beforeEntryID
        var retrieveCount = 0
        var failCount = 0

        while (limit?.let { retrieveCount < it } != false) {
            val batch: Int = when {
                limit == null -> apiLimit
                (limit - retrieveCount) in 1..apiLimit -> limit - retrieveCount
                else -> apiLimit // TODO Check this math
            }
            val entries = guild.context.requester.sendRequest(
                Route.GetGuildAuditLog(guild.id, userID, eventType, before, batch)
            ).value
                ?.audit_log_entries
                ?.map { it.toAuditLogEntry(guildData) }


            if (entries == null) failCount++
            else if (entries.isEmpty()) break
            else {
                retrieveCount += batch
                before = entries.last().id
                entries.forEach { emit(it) }
            }
        }
    }.also { f -> collector?.run { f.collect { invoke(it) } } }

}

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
        fun byID(int: Int) = map[int]
    }
}

internal suspend fun AuditLogPacket.toAuditLog(guildData: GuildData): AuditLog = AuditLog(
    guildData,
    audit_log_entries.map { it.toAuditLogEntry(guildData) },
    webhooks.map { it.id },
    users.map { it.id }.toSet(),
    users.mapNotNull { guildData.getMemberData(it.id)?.lazyMember }.toSet()
)

internal fun EntryPacket.toAuditLogEntry(guildData: GuildData): AuditLogEntry = AuditLogEntry(
    id,
    target_id,
    AuditLogEvent.byID(action_type)!!,
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
internal fun ChangePacket.toAuditLogEntryChange() = when (keyType) {
    ChangePacket.Key.GuildName -> EntryChange.GuildName(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GuildIconHash -> EntryChange.GuildIconHash(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GuildSplashHash -> EntryChange.GuildSplashHash(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GuildOwnerID -> EntryChange.GuildOwnerID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.GuildRegion -> EntryChange.GuildRegion(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GuildAfkChannelID -> EntryChange.GuildAfkChannelID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.GuildAfkTimeout -> EntryChange.GuildAfkTimeout(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.GuildMfaLevel -> EntryChange.GuildMfaLevel(
        old_value?.primitive?.intOrNull?.let { MfaLevel.values()[it] },
        new_value?.primitive?.intOrNull?.let { MfaLevel.values()[it] }
    )
    ChangePacket.Key.GuildVerificationLevel -> EntryChange.GuildVerificationLevel(
        old_value?.primitive?.intOrNull?.let { VerificationLevel.values()[it] },
        new_value?.primitive?.intOrNull?.let { VerificationLevel.values()[it] }
    )
    ChangePacket.Key.GuildContentFilter -> EntryChange.GuildExplicitContentFilterLevel(
        old_value?.primitive?.intOrNull?.let { ExplicitContentFilterLevel.values()[it] },
        new_value?.primitive?.intOrNull?.let { ExplicitContentFilterLevel.values()[it] }
    )
    ChangePacket.Key.GuildDefaultMessageNotification -> EntryChange.GuildMessageNotificationLevel(
        old_value?.primitive?.intOrNull?.let { MessageNotificationLevel.values()[it] },
        new_value?.primitive?.intOrNull?.let { MessageNotificationLevel.values()[it] }
    )
    ChangePacket.Key.GuildVanityUrl -> EntryChange.GuildVanityUrl(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GuildRoleAdd -> EntryChange.GuildRoleAdd(
        old_value?.jsonArray?.mapNotNull { it.jsonObject["id"]?.primitive?.longOrNull },
        new_value?.jsonArray?.mapNotNull { it.jsonObject["id"]?.primitive?.longOrNull }
    )
    ChangePacket.Key.GuildRoleRemove -> EntryChange.GuildRoleRemove(
        old_value?.jsonArray?.mapNotNull { it.jsonObject["id"]?.primitive?.longOrNull },
        new_value?.jsonArray?.mapNotNull { it.jsonObject["id"]?.primitive?.longOrNull }
    )
    ChangePacket.Key.GuildRolePermissions -> EntryChange.GuildRolePermissions(
        old_value?.primitive?.intOrNull?.toPermissions(),
        new_value?.primitive?.intOrNull?.toPermissions()
    )
    ChangePacket.Key.GuildRoleColor -> EntryChange.GuildRoleColor(
        old_value?.primitive?.intOrNull?.let { Color(it) },
        new_value?.primitive?.intOrNull?.let { Color(it) }
    )
    ChangePacket.Key.GuildRoleHoist -> EntryChange.GuildRoleHoist(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.GuildRoleMentionable -> EntryChange.GuildRoleMentionable(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.GuildRoleAllow -> EntryChange.GuildRoleAllow(
        old_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull(),
        new_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull()
    )
    ChangePacket.Key.GuildRoleDeny -> EntryChange.GuildRoleDeny(
        old_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull(),
        new_value?.primitive?.intOrNull?.toPermissions()?.firstOrNull()
    )
    ChangePacket.Key.GuildPruneDays -> EntryChange.GuildPruneDays(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.GuildWidgetEnabled -> EntryChange.GuildWidgetEnabled(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.GuildWidgetChannelID -> EntryChange.GuildWidgetChannelID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.ChannelPosition -> EntryChange.ChannelPosition(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.ChannelTopic -> EntryChange.ChannelTopic(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.ChannelBitrate -> EntryChange.ChannelBitrate(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.ChannelPermissionOverwrites -> EntryChange.ChannelPermissionOverwrites(
        old_value?.jsonArray?.mapNotNull {
            Json.parse(PermissionOverwritePacket.serializer(), it.toString()).toOverride()
        },
        new_value?.jsonArray?.mapNotNull {
            Json.parse(PermissionOverwritePacket.serializer(), it.toString()).toOverride()
        }
    )
    ChangePacket.Key.ChannelNsfw -> EntryChange.ChannelNsfw(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.ChannelApplicationID -> EntryChange.ChannelApplicationID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.InviteCode -> EntryChange.InviteCode(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.InviteChannelID -> EntryChange.InviteChannelID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.InviterID -> EntryChange.InviterID(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.InviteMaxUses -> EntryChange.InviteMaxUses(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.InviteUses -> EntryChange.InviteUses(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.InviteMaxAge -> EntryChange.InviteMaxAge(
        old_value?.primitive?.intOrNull, new_value?.primitive?.intOrNull
    )
    ChangePacket.Key.InviteTemporary -> EntryChange.InviteTemporary(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.UserDeafenState -> EntryChange.UserDeafenState(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.UserMuteState -> EntryChange.UserMuteState(
        old_value?.primitive?.booleanOrNull, new_value?.primitive?.booleanOrNull
    )
    ChangePacket.Key.UserNickname -> EntryChange.UserNickname(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.UserAvatarHash -> EntryChange.UserAvatarHash(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    ChangePacket.Key.GenericSnowflake -> EntryChange.GenericSnowflake(
        old_value?.primitive?.longOrNull, new_value?.primitive?.longOrNull
    )
    ChangePacket.Key.Type -> EntryChange.Type(
        old_value?.primitive?.contentOrNull, new_value?.primitive?.contentOrNull
    )
    else -> error("Audit Change Key type not found")
}
