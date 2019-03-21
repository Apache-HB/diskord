package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.GuildMember
import com.serebit.strife.entities.User

interface GuildEvent : Event {
    val guild: Guild?
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
 */
class GuildDeleteEvent internal constructor(
    override val context: Context,
    val guildID: Long,
    val wasKicked: Boolean
) : GuildEvent {
    override val guild: Guild? = null
}

interface GuildBanEvent : GuildEvent {
    val user: User
}

class GuildBanAddEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent

class GuildBanRemoveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val user: User
) : GuildBanEvent


interface GuildMemberEvent : GuildEvent {
    val member: GuildMember?
}

class GuildMemberJoinEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent

class GuildMemberLeaveEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    val user: User
) : GuildMemberEvent {
    override val member: GuildMember? = null
}

class GuildMemberUpdateEvent internal constructor(
    override val context: Context,
    override val guild: Guild,
    override val member: GuildMember
) : GuildMemberEvent
