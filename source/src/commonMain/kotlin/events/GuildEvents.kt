package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.internal.caching.plusAssign
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.UnavailableGuildPacket

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
