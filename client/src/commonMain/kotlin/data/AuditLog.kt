package com.serebit.strife.data

import com.serebit.strife.data.AuditLog.AuditLogEntry
import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryChange
import com.serebit.strife.data.AuditLog.AuditLogEntry.EntryInfo.OverwriteInfo.EntryOverwriteType
import com.serebit.strife.entities.*
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.packets.AuditLogPacket
import com.serebit.strife.internal.packets.AuditLogPacket.ChangePacket
import com.serebit.strife.internal.packets.AuditLogPacket.EntryPacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.UnstableDefault

/**
 * The [AuditLog] is the ledger of a [Guild]; it contains any administrative action performed in a list of [entries].
 *
 * @property guild The [Guild] this [AuditLog] is from.
 * @property entries The list of [Entries][AuditLog.AuditLogEntry] in this [AuditLog]. This list holds only the most recent 100
 * entries, use [getHistory][AuditLog.getHistory] function to get a flow of all entries.
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
     * (e.g. the user is no longer in the [getGuild]).
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
            data class PruneInfo(
                val inactiveMemberDays: Int? = null,
                val pruneResult: Int? = null
            ) : EntryInfo()

            /**
             * @property channelID channel in which messages were deleted.
             * @property deleteCount number of deleted messages in [channelID].
             */
            data class MessageDeleteInfo(
                val channelID: Long? = null,
                val deleteCount: Int? = null
            ) : EntryInfo()

            /**
             * Additional Information about an [AuditLogEvent.CHANNEL_OVERWRITE_CREATE],
             * [AuditLogEvent.CHANNEL_OVERWRITE_UPDATE], or [AuditLogEvent.CHANNEL_OVERWRITE_DELETE].
             *
             * @property overwrittenID id of an overwritten entity. Applicable for
             * @property overwrittenType The type of overwritten entity. Either [EntryOverwriteType.MEMBER] or
             * [EntryOverwriteType.ROLE].
             * @property roleName The name of the [GuildRole] if [overwrittenType] is [EntryOverwriteType.ROLE].
             */
            data class OverwriteInfo(
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
            /** Represents a [Guild]'s name being changed. */
            class GuildName internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild]'s icon being changed. */
            class GuildIconHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild]'s splash image being changed. */
            class GuildSplashHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild]'s owner transferring ownership to another member. */
            class GuildOwnerID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Guild]'s voice server region being changed. */
            class GuildRegion internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [Guild]'s AFK voice channel being changed. */
            class GuildAfkChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [Guild]'s AFK voice channel timeout being changed. */
            class GuildAfkTimeout internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [Guild]'s multi-factor authentication setting being changed. */
            class GuildMfaLevel internal constructor(old: MfaLevel?, new: MfaLevel?) : EntryChange<MfaLevel>(old, new)

            /** Represents a [Guild]'s required verification level being changed. */
            class GuildVerificationLevel internal constructor(old: VerificationLevel?, new: VerificationLevel?) :
                EntryChange<VerificationLevel>(old, new)

            /** Represents a [Guild]'s explicit content filtering setting being changed. */
            class GuildExplicitContentFilterLevel internal constructor(
                old: ExplicitContentFilterLevel?, new: ExplicitContentFilterLevel?
            ) : EntryChange<ExplicitContentFilterLevel>(old, new)

            /** Represents a [Guild] setting a new level for message notifications. */
            class GuildMessageNotificationLevel internal constructor(
                old: MessageNotificationLevel?, new: MessageNotificationLevel?
            ) : EntryChange<MessageNotificationLevel>(old, new)

            /** Represents a [Guild]'s vanity link being changed (such as `discord.gg/tft`). */
            class GuildVanityUrl internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [GuildRole] being added. */
            class GuildRoleAdd internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** Represents a [GuildRole] being removed. */
            class GuildRoleRemove internal constructor(old: List<Long>?, new: List<Long>?) :
                EntryChange<List<Long>>(old, new)

            /** Represents a [GuildRole]'s assigned permissions being changed. */
            class GuildRolePermissions internal constructor(old: Set<Permission>?, new: Set<Permission>?) :
                EntryChange<Set<Permission>>(old, new)

            /** Represents a [GuildRole]'s color being changed. */
            class GuildRoleColor internal constructor(old: Color?, new: Color?) : EntryChange<Color>(old, new)

            /** Represents a [GuildRole]'s hoist status being changed. */
            class GuildRoleHoist internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildRole]'s ability to be mentioned being changed. */
            class GuildRoleMentionable internal constructor(old: Boolean?, new: Boolean?) :
                EntryChange<Boolean>(old, new)

            /** Represents a [Permission] being allowed for a [GuildRole]. */
            class GuildRoleAllow internal constructor(old: Permission?, new: Permission?) :
                EntryChange<Permission>(old, new)

            /** Represents a [Permission] being denied for a [GuildRole]. */
            class GuildRoleDeny internal constructor(old: Permission?, new: Permission?) : EntryChange<Permission>(old, new)

            /** Represents the number of days after which inactive and role-unassigned [GuildMember]s are kicked. */
            class GuildPruneDays internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildEmbed] being enabled or disabled. */
            class GuildWidgetEnabled internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildEmbed]'s linked channel ID being changed. */
            class GuildWidgetChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents a [GuildChannel]'s position in the sidebar being changed. */
            class ChannelPosition internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildMessageChannel]'s topic being changed. */
            class ChannelTopic internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [GuildVoiceChannel]'s bitrate being changed. */
            class ChannelBitrate internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents a [GuildChannel]'s permission overrides being changed. */
            class ChannelPermissionOverwrites internal constructor(
                old: List<PermissionOverride>?, new: List<PermissionOverride>?
            ) : EntryChange<List<PermissionOverride>>(old, new)

            /** Represents a [GuildMessageChannel]'s NSFW setting being changed. */
            class ChannelNsfw internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildTextChannel]'s application being changed. */
            class ChannelApplicationID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents an [Invite]'s code being changed. */
            class InviteCode internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents an [Invite]'s channel being changed. */
            class InviteChannelID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents an [Invite]'s inviter being changed. */
            class InviterID internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Represents an [Invite]'s use limit being changed. */
            class InviteMaxUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents an [Invite]'s use count being updated. */
            class InviteUses internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents an [Invite] being changed. */
            class InviteMaxAge internal constructor(old: Int?, new: Int?) : EntryChange<Int>(old, new)

            /** Represents an [Invite]'s temporary setting being changed. */
            class InviteTemporary internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember]'s state of being deafened being changed. */
            class UserDeafenState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember]'s state of being muted being changed. */
            class UserMuteState internal constructor(old: Boolean?, new: Boolean?) : EntryChange<Boolean>(old, new)

            /** Represents a [GuildMember]'s nickname being changed. */
            class UserNickname internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /** Represents a [User]'s avatar being changed. */
            class UserAvatarHash internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)

            /**
             * Represents any snowflake ID of a changed entity. Sometimes used in conjunction with other entry changes.
             */
            class GenericSnowflake internal constructor(old: Long?, new: Long?) : EntryChange<Long>(old, new)

            /** Any	integer (channel type) or string type of entity created */
            class Type internal constructor(old: String?, new: String?) : EntryChange<String>(old, new)
        }
    }

    val guild: Guild get() = guildData.lazyEntity

    /**
     * Returns a flow of [AuditLogEntry]. The flow can be filtered with these optional parameters:
     *
     * [limit]: maximum number of [AuditLogEntry] to retrieve.
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
    MEMBER_DISCONNECT(27),
    MEMBER_MOVE(26),
    BOT_ADD(28),
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
    MESSAGE_DELETE(72),
    MESSAGE_BULK_DELETE(73),
    MESSAGE_PIN(74),
    MESSAGE_UNPIN(75),
    INTEGRATION_CREATE(80),
    INTEGRATION_UPDATE(81),
    INTEGRATION_DELET(82);

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

internal fun EntryPacket.toAuditLogEntry(guildData: GuildData): AuditLogEntry =
    AuditLogEntry(
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
    delete_member_days != null -> AuditLogEntry.EntryInfo.PruneInfo(
        delete_member_days,
        members_removed
    )
    channel_id != null -> AuditLogEntry.EntryInfo.MessageDeleteInfo(channel_id, count)
    id != null -> AuditLogEntry.EntryInfo.OverwriteInfo(
        id,
        type?.toUpperCase()?.let { EntryOverwriteType.valueOf(it) },
        role_name
    )
    else -> AuditLogEntry.EntryInfo.UnknownInfoType
}

@OptIn(UnstableDefault::class)
internal fun ChangePacket.toAuditLogEntryChange() =
    keyType?.toEntryChange(this) ?: error("Audit Change Key type not found")
