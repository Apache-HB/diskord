package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.internal.cacheAll
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket

class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : Event {
    init {
        context.cache.cacheAll(packet.members.map { it.user.toData(context) })
        context.cache.cache(packet.toData(context))
    }
}

class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : Event {
    init {
        context.cache.update(packet)
    }
}

class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildId: Long = packet.id
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.cache.removeGuild(packet.id)
    }
}
