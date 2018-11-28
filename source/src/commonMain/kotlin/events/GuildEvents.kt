package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.internal.caching.plusAssign
import com.serebit.diskord.internal.entitydata.toData
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket

class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : Event {
    init {
        context.userCache += packet.members.map { it.user.toData(context) }
        context.guildCache += packet.toData(context)
    }
}

class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : Event {
    init {
        context.guildCache[packet.id]?.update(packet)
    }
}

class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildId: Long = packet.id
    val wasKicked: Boolean = packet.unavailable == null

    init {
        context.guildCache -= packet.id
    }
}
