package com.serebit.strife.events

import com.serebit.strife.BotClient
import com.serebit.strife.data.Presence
import com.serebit.strife.entities.*

/** Any Event involving a [Guild] entity. */
interface GuildEvent : Event {
    /** The relevant [Guild]. */
    val guild: Guild
}

/**
 * This event can be sent in three different scenarios:
 *
 * - When a user is initially connecting, to lazily load and backfill information for all unavailable guilds sent in
 * the Ready event.
 * - When a Guild becomes available again to the client.
 * - When the current user joins a new Guild.
 *
 * @property guild The Created [Guild].
 */
class GuildCreateEvent internal constructor(override val context: BotClient, override val guild: Guild) : GuildEvent

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: BotClient, override val guild: Guild) : GuildEvent

/**
 * Sent when a [Guild] becomes unavailable during a [Guild] outage,
 * or when the client leaves or is removed from a guild.
 *
 * @property guildID The ID of the deleted [Guild].
 * @property wasKicked `true` if the client was kicked from the [Guild].
 */
class GuildDeleteEvent internal constructor(
    override val context: BotClient,
    val guildID: Long,
    val wasKicked: Boolean
) : Event

/**
 * Sent when a [User] is banned or unbanned from [Guild].
 * @property user The relevant [User].
 */
interface GuildBanEvent : GuildEvent {
    val user: User
}

/** Sent when a [user] is banned from the [guild]. */
class GuildBanAddEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent

/** Sent when a [user] is unbanned from the [guild]. */
class GuildBanRemoveEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent

/** Sent when a [guild] has updated its [emojis]. */
class GuildEmojisUpdateEvent(
    override val context: BotClient,
    override val guild: Guild,
    /** The emojis that the guild now contains. I think. Discord docs aren't very specific. */
    val emojis: List<GuildEmoji>
) : GuildEvent

/** Any [GuildEvent] involving a [GuildMember]. */
interface GuildMemberEvent : GuildEvent {
    /** The relevant [GuildMember]. */
    val member: GuildMember
}

/**
 * Received when a [User] joins a [Guild].
 *
 * @property member The [GuildMember] who joined.
 */
class GuildMemberJoinEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent

/**
 * Received when a [User] leaves a [Guild].
 *
 * @property user The [User] who left the [guild]
 */
class GuildMemberLeaveEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    val user: User
) : GuildEvent

/**
 * Received when a [GuildMember] has it's information updated.
 *
 * @property member The updated [GuildMember]
 */
class GuildMemberUpdateEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent

/** Received when a [guild]'s integrations have been updated. */
class GuildIntegrationsUpdateEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild
) : GuildEvent

/**
 * Received when Discord send us the requested [members] in a [guild].
 *
 * @property members The members in this chunk.
 */
class GuildMembersChunkEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    val members: List<GuildMember>
) : GuildEvent

/** Received when a [role] has been created, updated or deleted. */
interface GuildRoleEvent : GuildEvent {
    /** The [GuildRole] that this event applies to. */
    val role: GuildRole?
}

/** Received when a [role] has been created in a [guild]. */
class GuildRoleCreateEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val role: GuildRole
) : GuildRoleEvent

/** Received when a [role] has been updated in a [guild]. */
class GuildRoleUpdateEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    override val role: GuildRole
) : GuildRoleEvent

/**
 * Received when a [role] has been deleted in a [guild], with the [id][roleID] of the deleted role.
 *
 * @property roleID The ID of the deleted role.
 */
class GuildRoleDeleteEvent internal constructor(
    override val context: BotClient,
    override val guild: Guild,
    val roleID: Long
) : GuildRoleEvent {
    override val role: GuildRole? = null
}

/**
 * Sent when a [User]'s [Presence] or info, such as name or avatar, is updated.
 *
 * @property guild The [Guild] in which the update took place.
 * @property member The [GuildMember] whose information was updated.
 * @property presence The [User]'s new [Presence].
 */
class PresenceUpdateEvent(
    override val context: BotClient,
    override val guild: Guild,
    val member: GuildMember,
    val presence: Presence
) : GuildEvent
