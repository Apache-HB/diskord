package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket

/**
 * This event can be sent in three different scenarios:
 *
 * - When a user is initially connecting, to lazily load and backfill
 * information for all unavailable guilds sent in the Ready event.
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

/**
 * Sent when a guild is updated. (TODO better docs. Thanks, Discord...)
 */
class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : Event {
    init {
        context.guildCache[packet.id]?.update(packet)
    }
}

/**
 * Sent when a guild becomes unavailable during a guild outage, or when the
 * client leaves or is removed from a guild.
 */
class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildId: Long = packet.id
    /** `true` if the bot-client was kicked from this [Guild] */
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.guildCache -= packet.id
    }
}
