package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.data.Member
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.User
import com.serebit.strife.internal.dispatches.GuildBanAdd
import com.serebit.strife.internal.dispatches.GuildBanRemove
import com.serebit.strife.internal.dispatches.GuildMemberRemove
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.MemberPacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket

interface GuildEvent : Event {
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
    override val guild = context.cache.pushGuildData(packet).toEntity()
}

/** Sent when a guild is updated. (TODO better docs. Thanks, Discord...) */
class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : GuildEvent {
    override val guild = context.cache.pullGuildData(packet).toEntity()
}

/**
 * Sent when a guild becomes unavailable during a guild outage, or when the client leaves or is removed from a guild.
 */
class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildID: Long = packet.id
    /** `true` if the bot-client was kicked from this [guild][com.serebit.strife.entities.Guild]. */
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.cache.decache(packet.id)
    }
}

class GuildBanAddEvent internal constructor(override val context: Context, packet: GuildBanAdd.Data) : GuildEvent {
    override val guild = context.cache.getGuildData(packet.guild_id)!!.toEntity()
    val user: User = context.cache.pullUserData(packet.user).toEntity()
}

class GuildBanRemoveEvent internal constructor(
    override val context: Context, packet: GuildBanRemove.Data
) : GuildEvent {
    override val guild = context.cache.getGuildData(packet.guild_id)!!.toEntity()
    val user: User = context.cache.pullUserData(packet.user).toEntity()
}

class GuildMemberJoinEvent internal constructor(override val context: Context, packet: MemberPacket) : GuildEvent {
    private val guildData = context.cache.getGuildData(packet.guild_id!!)!!
    val member: Member = Member(packet, guildData, context)
    override val guild = guildData.also { it.members += member }.toEntity()
}

class GuildMemberLeaveEvent internal constructor(
    override val context: Context, packet: GuildMemberRemove.Data
) : GuildEvent {
    override val guild = context.cache.getGuildData(packet.guild_id)!!.also { data ->
        data.members.removeAll { it.user.id == user.id }
    }.toEntity()
    val user: User = context.cache.pullUserData(packet.user).toEntity()
}
