package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.data.Member
import com.serebit.strife.entities.User
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.dispatches.GuildBanAdd
import com.serebit.strife.internal.dispatches.GuildBanRemove
import com.serebit.strife.internal.dispatches.GuildMemberRemove
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.MemberPacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket

/**
 * This event can be sent in three different scenarios:
 *
 * - When a user is initially connecting, to lazily load and backfill information for all unavailable guilds sent in
 * the Ready event.
 * - When a Guild becomes available again to the client.
 * - When the current user joins a new Guild.
 */
class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : Event {
    init {
        // Update user cache
        context.userCache.putAll(packet.members.map { it.user.toData(context) }.associateBy { it.id })
        // Update guild Cache
        context.guildCache += (packet.id to packet.toData(context))
    }
}

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : Event {
    init {
        context.guildCache[packet.id]?.update(packet)
    }
}

/**
 * Sent when a guild becomes unavailable during a guild outage, or when the client leaves or is removed from a guild.
 */
class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildID: Long = packet.id
    /** `true` if the bot-client was kicked from this [guild][com.serebit.strife.entities.Guild]. */
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.guildCache -= packet.id
    }
}

class GuildBanAddEvent internal constructor(override val context: Context, packet: GuildBanAdd.Data) : Event {
    val guildID: Long = packet.guild_id
    val user: User = context.getUserData(packet.user.id)
        ?.update(packet.user)
        ?.toUser()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toUser()

    init {
        context.guildCache[guildID]?.members?.removeAll { it.user.id == user.id }
    }
}

class GuildBanRemoveEvent internal constructor(override val context: Context, packet: GuildBanRemove.Data) : Event {
    val guildID: Long = packet.guild_id
    val user: User = context.getUserData(packet.user.id)
        ?.update(packet.user)
        ?.toUser()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toUser()
}

class GuildMemberJoinEvent internal constructor(override val context: Context, packet: MemberPacket) : Event {
    val guildID: Long = packet.guild_id!! // always sent with join packets
    private val guildData = context.guildCache[guildID]!!
    val member = Member(packet, guildData, context).also { guildData.members += it }
}

class GuildMemberLeaveEvent internal constructor(
    override val context: Context, packet: GuildMemberRemove.Data
) : Event {
    val guildID: Long = packet.guild_id
    val user: User = context.getUserData(packet.user.id)
        ?.update(packet.user)
        ?.toUser()
        ?: packet.user.toData(context)
            .also { context.userCache[it.id] = it }
            .toUser()

    init {
        context.guildCache[guildID]?.members?.removeAll { it.user.id == user.id }
    }
}
