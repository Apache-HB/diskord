package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildPacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket

class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : Event {
    val guild = Guild(packet).cache()

    init {
        EntityCache.guildCreatePackets[packet.id] = packet
    }
}

class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildPacket) : Event {
    val guild = Guild(EntityCache.guildCreatePackets[packet.id]!!.update(packet)).cache()
}

class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildId: Long = packet.id
    val wasKicked: Boolean = packet.unavailable == null
}
