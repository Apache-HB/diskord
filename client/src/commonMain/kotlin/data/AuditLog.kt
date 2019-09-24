package com.serebit.strife.data

import com.serebit.strife.BotClient
import com.serebit.strife.data.AuditLog.AuditLogEntry
import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryChange
import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryInfo.EntryOverwriteType
import com.serebit.strife.entities.*
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildMessageChannelData
import com.serebit.strife.internal.packets.AuditLogPacket
import com.serebit.strife.internal.packets.AuditLogPacket.ChangePacket
import com.serebit.strife.internal.packets.AuditLogPacket.EntryPacket
import kotlinx.coroutines.flow.Flow

/**
 * The [AuditLog] is the ledger of a [Guild]; it contains any administrative action performed in a list of [entries].
 *
 * @property entries The list of [Entires][AuditLog.Entry] in this [AuditLog], this list holds only the most recent 100
 * entires, use [getHistory][AuditLog.getHistory] function to get a [Flow] of all entries.
 * @property webhooks A list of [Webhook]s found in the [AuditLog].
 * @property users A list of [User]s found in the [AuditLog].
 */
data class AuditLog(
    val guild: Guild,
    val entries: List<AuditLogEntry> = emptyList(),
    val webhooks: List<Webhook> = emptyList(),
    val members: List<GuildMember> = emptyList()
) {

    /**
     * Whenever an admin action is performed on the API, an entry is added to the respective guild's audit log.
     *
     * @property targetID [ID][Entity.id] of the affected [Entity]. (webhook, user, role, etc.)
     */
    data class AuditLogEntry(
        val id: Long,
        val targetID: Long?,
        val type: AuditLogEvent,
        val userID: Long,
        val member: GuildMember?,
        val changes: List<EntryChange<*>>,
        val extraInfo: EntryInfo?,
        val reason: String?
    ) {

        /**
         * Additional information regarding an [AuditLogEntry].
         *
         * @property inactiveMemberDays number of days after which inactive members were kicked.
         * Applicable for [AuditLogEvent.MEMBER_PRUNE].
         * @property pruneResult number of members removed by a prune. Applicable for [AuditLogEvent.MEMBER_PRUNE].
         * @property channelID channel in which messages were deleted. Applicable for [AuditLogEvent.MESSAGE_DELETE].
         * @property deleteCount number of deleted messages in [channelID].
         * Applicable for [AuditLogEvent.MESSAGE_DELETE].
         * @property overwrittenID id of an overwritten entity. Applicable for
         * [AuditLogEvent.CHANNEL_OVERWRITE_CREATE], [AuditLogEvent.CHANNEL_OVERWRITE_UPDATE],
         * and [AuditLogEvent.CHANNEL_OVERWRITE_DELETE].
         * @property overwrittenType The type of overwritten entity. Either [EntryOverwriteType.MEMBER] or
         * [EntryOverwriteType.ROLE]. Applicable for [AuditLogEvent.CHANNEL_OVERWRITE_CREATE],
         * [AuditLogEvent.CHANNEL_OVERWRITE_UPDATE], and [AuditLogEvent.CHANNEL_OVERWRITE_DELETE].
         * @property roleName The name of the [GuildRole] if [overwrittenType] is [EntryOverwriteType.ROLE].
         */
        data class EntryInfo(
            val inactiveMemberDays: Int,
            val pruneResult: Int,
            val channelID: Long,
            val deleteCount: Int,
            val overwrittenID: Long,
            val overwrittenType: EntryOverwriteType,
            val roleName: String
        ) {
            /** Used for [EntryInfo.overwrittenType] */
            enum class EntryOverwriteType {
                MEMBER, ROLE
            }
        }

        sealed class EntryChange<T>(val oldValue: T? = null, val newValue: T? = null) {

            /** Guild name changed */
            class GuildName(old: String?, new: String?) : EntryChange<String>(old, new)

            /**	guild	string	icon changed */
            class GuildIconHash(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	string	invite splash page artwork changed */
            class GuildSplashHash(old: String?, new: String?) : EntryChange<String>(old, new)

            /**	guild	snowflake	owner changed */
            class GuildOwnerID(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /**	guild	string	region changed */
            class GuildRegion(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	snowflake	afk channel changed */
            class GuildAfkChannelID(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** guild	integer	afk timeout duration changed */
            class GuildAfkTimeout(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** guild	integer	two-factor auth requirement changed */
            class GuildMfaLevel(old: MfaLevel?, new: MfaLevel?) : EntryChange<MfaLevel>(old, new)

            /** guild	integer	required verification level changed */
            class GuildVerificationLevel(old: VerificationLevel?, new: VerificationLevel?) :
                EntryChange<VerificationLevel>(old, new)

            /** guild	integer	change in whose messages are scanned and deleted for explicit content in the server */
            class GuildExplicitContentFilterLevel(old: ExplicitContentFilterLevel?, new: ExplicitContentFilterLevel?) :
                EntryChange<ExplicitContentFilterLevel>(old, new)

            /** guild	integer	default message notification level changed */
            class GuildMessageNotificationLevel(old: MessageNotificationLevel?, new: MessageNotificationLevel?) :
                EntryChange<MessageNotificationLevel>(old, new)

            /** guild	string	guild invite vanity url changed */
            class GuildVanityUrl(old: String?, new: String?) : EntryChange<String>(old, new)

            /** guild	array of role classs	new role added */
            class GuildRoleAdd(old: List<GuildRole>?, new: List<GuildRole>?) : EntryChange<List<GuildRole>>(old, new)

            /** guild	array of role classs	role removed */
            class GuildRoleRemove(old: List<GuildRole>?, new: List<GuildRole>?) : EntryChange<List<GuildRole>>(old, new)

            /** role	integer	permissions for a role changed */
            class GuildRolePermissions(old: List<Permission>?, new: List<Permission>?) :
                EntryChange<List<Permission>>(old, new)

            /** role	integer	role color changed */
            class GuildRoleColor(old: Color?, new: Color?) : EntryChange<Color>(old, new)

            /** role	boolean	role is now displayed/no longer displayed separate from online users */
            class GuildRoleHoist(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** role	boolean	role is now mentionable/unmentionable */
            class GulidRoleMentionable(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** role	integer	a permission on a text or voice channel was allowed for a role */
            class GuildRoleAllow(old: List<PermissionOverride>?, new: List<PermissionOverride>?) :
                EntryChange<List<PermissionOverride>>(old, new)

            /** role	integer	a permission on a text or voice channel was allowed for a role */
            class GuildRoleDeny(old: List<PermissionOverride>?, new: List<PermissionOverride>?) :
                EntryChange<List<PermissionOverride>>(old, new)

            /** guild	integer	change in number of days after which inactive and role-unassigned members are kicked*/
            class GuildPruneDays(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** guild	boolean	server widget enabled/disable */
            class GuildWidgetEnabled(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** guild	snowflake	channel id of the server widget changed */
            class GuildWidgetChannelID(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** channel	integer	text or voice channel position changed */
            class ChannelPosition(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** channel	string	text channel topic changed */
            class ChannelTopic(old: String?, new: String?) : EntryChange<String>(old, new)

            /** channel	integer	voice channel bitrate changed */
            class ChannelBitrate(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** channel	array of channel overwrite classs	permissions on a channel changed */
            class ChannelPermissionOverwrites(old: List<PermissionOverride>?, new: List<PermissionOverride>) :
                EntryChange<List<PermissionOverride>>(old, new)

            /** channel	boolean	channel nsfw restriction changed */
            class ChannelNsfw(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** channel	snowflake	application id of the added or removed webhook or bot */
            class ChannelApplicationID(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** invite	string	invite code changed */
            class InviteCode(old: String?, new: String?) : EntryChange<String>(old, new)

            /** invite	snowflake	channel for invite code changed */
            class InviteChannelID(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** invite	snowflake	person who created invite code changed */
            class InviterID(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** invite	integer	change to max number of times invite code can be used */
            class InviteMaxUsers(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	integer	number of times invite code used changed */
            class InviteUses(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	integer	how long invite code lasts changed. See [Invite.activeTimeRange]. */
            class InviteMaxAge(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** invite	boolean	invite code is temporary/never expires */
            class InviteTemporary(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	boolean	user server deafened/undeafened */
            class UserDeafenState(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	boolean	user server muted/unmuted */
            class UserMuteState(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** user	string	user nickname changed */
            class UserNickname(old: String?, new: String?) : EntryChange<String>(old, new)

            /** user	string	user avatar changed */
            class UserAvatarHash(old: String?, new: String?) : EntryChange<String>(old, new)

            /** any	snowflake	the id of the changed entity - sometimes used in conjunction with other keys */
            class GenericSnowflake(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** any	integer (channel type) or string	type of entity created */
            class Type(old: String?, new: String?) : EntryChange<String>(old, new)
        }
    }


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
        beforeEntryID: Long
    ): Flow<AuditLogEntry> = TODO()

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
        fun byID(int: Int) = values().find { it.id == int }
    }
}

internal suspend fun AuditLogPacket.toAuditLog(guildData: GuildData, context: BotClient): AuditLog = AuditLog(
    guildData.lazyEntity,
    audit_log_entries.map { it.toAuditLogEntry(guildData) },
    webhooks.map {
        it.toEntity(
            context, guildData, guildData.getChannelData(it.channel_id)!! as GuildMessageChannelData<*, *>
        )
    },
    users.mapNotNull { guildData.getMemberData(it.id)?.lazyMember }
)

internal suspend fun EntryPacket.toAuditLogEntry(guildData: GuildData): AuditLogEntry = AuditLogEntry(
    id,
    target_id,
    AuditLogEvent.byID(action_type)!!,
    user_id,
    guildData.getMemberData(user_id)?.lazyMember,
    changes?.map { it.toAuditLogEntryChange(guildData) } ?: emptyList<EntryChange<*>>(),
    options?.toEntryInfo(),
    reason
)

internal fun AuditLogPacket.OptionalEntryInfo.toEntryInfo() = AuditLogEntry.EntryInfo(
    delete_member_days,
    members_removed,
    channel_id,
    count,
    id,
    AuditLogEntry.EntryInfo.EntryOverwriteType.valueOf(type.toUpperCase()),
    role_name
)

internal suspend fun ChangePacket.toAuditLogEntryChange(guildData: GuildData) = when (key) {
    ChangePacket.Key.GuildName -> EntryChange.GuildName(old_value as String?, new_value as String?)
    ChangePacket.Key.GuildIconHash -> EntryChange.GuildIconHash(old_value as String?, new_value as String?)
    ChangePacket.Key.GuildSplashHash -> EntryChange.GuildSplashHash(old_value as String?, new_value as String?)
    ChangePacket.Key.GuildOwnerID -> EntryChange.GuildOwnerID(old_value as Long?, new_value as Long?)
    ChangePacket.Key.GuildRegion -> EntryChange.GuildRegion(old_value as String?, new_value as String?)
    ChangePacket.Key.GuildAfkChannelID -> EntryChange.GuildAfkChannelID(old_value as Long?, new_value as Long?)
    ChangePacket.Key.GuildAfkTimeout -> EntryChange.GuildAfkTimeout(old_value as Int?, new_value as Int?)
    ChangePacket.Key.GuildMfaLevel -> EntryChange.GuildMfaLevel(
        (old_value as? Int)?.let { MfaLevel.values()[it] },
        (new_value as? Int)?.let { MfaLevel.values()[it] }
    )
    ChangePacket.Key.GuildVerificationLevel -> EntryChange.GuildVerificationLevel(
        (old_value as? Int)?.let { VerificationLevel.values()[it] },
        (new_value as? Int)?.let { VerificationLevel.values()[it] }
    )
    ChangePacket.Key.GuildContentFilter -> EntryChange.GuildExplicitContentFilterLevel(
        (old_value as? Int)?.let { ExplicitContentFilterLevel.values()[it] },
        (new_value as? Int)?.let { ExplicitContentFilterLevel.values()[it] }
    )
    ChangePacket.Key.GuildDefaultMessageNotification -> EntryChange.GuildMessageNotificationLevel(
        (old_value as? Int)?.let { MessageNotificationLevel.values()[it] },
        (new_value as? Int)?.let { MessageNotificationLevel.values()[it] }
    )
    ChangePacket.Key.GuildVanityUrl -> EntryChange.GuildVanityUrl(old_value as String?, new_value as String?)
    ChangePacket.Key.GuildRoleAdd -> TODO()
    ChangePacket.Key.GuildRoleRemove -> TODO()
    ChangePacket.Key.GuildRolePermissions -> TODO()
    ChangePacket.Key.GuildRoleColor -> EntryChange.GuildRoleColor(
        (old_value as? Int)?.let { Color(it) }, (new_value as? Int)?.let { Color(it) }
    )
    ChangePacket.Key.GuildRoleHoist -> EntryChange.GuildRoleHoist(old_value as Boolean?, new_value as Boolean?)
    ChangePacket.Key.GulidRoleMentionable -> TODO()
    ChangePacket.Key.GuildRoleAllow -> TODO()
    ChangePacket.Key.GuildRoleDeny -> TODO()
    ChangePacket.Key.GuildPruneDays -> TODO()
    ChangePacket.Key.GuildWidgetEnabled -> TODO()
    ChangePacket.Key.GuildWidgetChannelID -> TODO()
    ChangePacket.Key.ChannelPosition -> TODO()
    ChangePacket.Key.ChannelTopic -> TODO()
    ChangePacket.Key.ChannelBitrate -> TODO()
    ChangePacket.Key.ChannelPermissionOverwrites -> TODO()
    ChangePacket.Key.ChannelNsfw -> TODO()
    ChangePacket.Key.ChannelApplicationID -> TODO()
    ChangePacket.Key.InviteCode -> TODO()
    ChangePacket.Key.InviteChannelID -> TODO()
    ChangePacket.Key.InviterID -> TODO()
    ChangePacket.Key.InviteMaxUsers -> TODO()
    ChangePacket.Key.InviteUses -> TODO()
    ChangePacket.Key.InviteMaxAge -> TODO()
    ChangePacket.Key.InviteTemporary -> TODO()
    ChangePacket.Key.UserDeafenState -> TODO()
    ChangePacket.Key.UserMuteState -> TODO()
    ChangePacket.Key.UserNickname -> TODO()
    ChangePacket.Key.UserAvatarHash -> TODO()
    ChangePacket.Key.GenericSnowflake -> TODO()
    ChangePacket.Key.Type -> TODO()
}
