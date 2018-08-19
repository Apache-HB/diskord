package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.Guild
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.GuildPacket

class GuildCreatedEvent internal constructor(override val context: Context, packet: GuildPacket) : Event {
    val guild = Guild(packet).cache()
}
