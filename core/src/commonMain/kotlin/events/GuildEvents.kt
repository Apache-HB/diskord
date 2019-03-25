package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.User

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
 */
class GuildCreateEvent internal constructor(override val context: Context, override val guild: Guild) : GuildEvent

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: Context, override val guild: Guild) : GuildEvent

/**
 * Sent when a guild becomes unavailable during a guild outage, or when the client leaves or is removed from a guild.
 * @property guildID The [ID][Guild.id] of the deleted [Guild].
 * @property wasKicked `true` If the bot client was kicked from the [Guild].
 */
class GuildDeleteEvent internal constructor(
    override val context: Context,
    val guildID: Long,
    val wasKicked: Boolean
) : Event

/**
 * Sent when a [User] is banned or UNbanned from [Guild].
 * @property user The relevant [User].
 */
interface GuildBanEvent : GuildEvent {
    val user: User
}

/** Sent when a the [user] is banned from the [guild]. */
class GuildBanAddEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent

/** Sent when a the [user] is unbanned from the [guild]. */
class GuildBanRemoveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent

/** Any [GuildEvent] involving a [GuildMember]. */
interface GuildMemberEvent : GuildEvent {
    /** The relevant [GuildMember]. */
    val member: GuildMember
}

class GuildMemberJoinEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent

/**
 * @property user The [User] who left.
 */
class GuildMemberLeaveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val user: User
) : GuildEvent

class GuildMemberUpdateEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent
