package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.data.Member
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.User
import com.serebit.strife.internal.dispatches.GuildBanAdd
import com.serebit.strife.internal.dispatches.GuildBanRemove
import com.serebit.strife.internal.dispatches.GuildMemberRemove
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.MemberPacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket

interface GuildEvent : Event {
    /** The relevant [Guild] */
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
class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : GuildEvent {
    /** The created [Guild]. */
    override val guild by lazy { context.guildCache[packet.id]!!.toEntity() }

    init {
        // Update user cache
        context.userCache.putAll(packet.members.map { it.user.toData(context) }.associateBy { it.id })
        // Update guild Cache
        context.guildCache[packet.id] = packet.toData(context)
    }
}

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : GuildEvent {
    override val guild by lazy { context.guildCache[packet.id]!!.toEntity() }

    init {
        // Update cached value
        context.guildCache[packet.id]?.update(packet)
    }
}

/** Sent when a [Guild] becomes unavailable during a outage, or when the client leaves or is removed from a [Guild]. */
class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    /** The ID of the deleted [Guild]. */
    val guildID: Long = packet.id
    /** `true` if the bot-client was kicked from this [guild][com.serebit.strife.entities.Guild]. */
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.guildCache -= packet.id
    }
}

/** Received when a [User] is banned from a [Guild]. */
class GuildBanAddEvent internal constructor(override val context: Context, packet: GuildBanAdd.Data) : GuildEvent {
    /** The relevant [Guild]. */
    override val guild by lazy { context.guildCache[packet.guild_id]!!.toEntity() }
    /** The banned [User]. */
    val user: User = context.getUserData(packet.user.id)
        ?.also { it.update(packet.user) }
        ?.toEntity()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toEntity()
}

/** Received when a [User] is unbanned from a [Guild]. */
class GuildBanRemoveEvent internal constructor(
    override val context: Context, packet: GuildBanRemove.Data
) : GuildEvent {
    /** The relevant [Guild]. */
    override val guild by lazy { context.guildCache[packet.guild_id]!!.toEntity() }
    /** The unbanned [User]. */
    val user: User = context.getUserData(packet.user.id)
        ?.also { it.update(packet.user) }
        ?.toEntity()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toEntity()
}

/** Received when a [User] joins a [Guild]. */
class GuildMemberJoinEvent internal constructor(override val context: Context, packet: MemberPacket) : GuildEvent {
    override val guild by lazy { context.guildCache[packet.guild_id]!!.toEntity() }
    /** The newly joined [Member]. */
    val member: Member

    init {
        context.guildCache[packet.guild_id]!!.let { data ->
            member = Member(packet, data, context).also { data.members += it }
        }
    }
}

/** Received when a [User] leaves a [Guild]. */
class GuildMemberLeaveEvent internal constructor(
    override val context: Context, packet: GuildMemberRemove.Data
) : GuildEvent {
    override val guild by lazy { context.guildCache[packet.guild_id]!!.toEntity() }
    /** The [User] who left. */
    val user: User = context.getUserData(packet.user.id)
        ?.also { it.update(packet.user) }
        ?.toEntity()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toEntity()

    init {
        // Remove the user from the cached Guild
        context.guildCache[packet.guild_id]?.members?.removeAll { it.user.id == user.id }
    }
}
