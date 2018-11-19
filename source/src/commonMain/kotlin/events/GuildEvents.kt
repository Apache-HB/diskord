package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.GuildCreatePacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket
import com.serebit.diskord.internal.packets.UnavailableGuildPacket

class GuildCreateEvent internal constructor(override val context: Context, packet: GuildCreatePacket) : Event {
    val guild = Guild(packet.cache().id)
}

class GuildUpdateEvent internal constructor(override val context: Context, packet: GuildUpdatePacket) : Event {
    private val originalPacket = EntityPacketCache.findId<GuildCreatePacket>(packet.id)!!
    val guild = Guild(originalPacket.update(packet).id)
}

class GuildDeleteEvent internal constructor(override val context: Context, packet: UnavailableGuildPacket) : Event {
    val guildId: Long = packet.id
    val wasKicked: Boolean = packet.unavailable == null
}
