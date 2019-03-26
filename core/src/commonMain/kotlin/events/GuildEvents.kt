package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.User

/** An [Event] involving a [Guild]. */
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
class GuildCreateEvent internal constructor(override val context: Context, override val guild: Guild) : GuildEvent

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: Context, override val guild: Guild) : GuildEvent

/**
 * Sent when a [Guild] becomes unavailable during a [Guild] outage,
 * or when the client leaves or is removed from a guild.
 *
 * @property guildID The ID of the deleted [Guild].
 * @property wasKicked `true` if the client was kicked from the [Guild].
 */
class GuildDeleteEvent internal constructor(
    override val context: Context,
    val guildID: Long,
    val wasKicked: Boolean
) : Event

/**
 * Received when a [User] is banned from a [Guild].
 *
 * @property user The banned [User]
 */
class GuildBanAddEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val user: User
) : GuildEvent

/**
 * Received when a [User] is unbanned from a [Guild].
 *
 * @property user The unbanned [User]
 */
class GuildBanRemoveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val user: User
) : GuildEvent

/**
 * Received when a [User] joins a [Guild].
 *
 * @property member The [GuildMember] who joined.
 */
class GuildMemberJoinEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val member: GuildMember
) : GuildEvent

/**
 * Received when a [User] leaves a [Guild].
 *
 * @property user The [User] who left the [guild]
 */
class GuildMemberLeaveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val user: User
) : GuildEvent

/**
 * Received when a [GuildMember] has it's information updated.
 *
 * @property member The updated [GuildMember]
 */
class GuildMemberUpdateEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val member: GuildMember
) : GuildEvent
