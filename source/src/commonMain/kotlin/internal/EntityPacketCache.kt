package com.serebit.diskord.internal

import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.EntityPacket
import com.serebit.diskord.internal.packets.GuildCreatePacket

internal object EntityPacketCache {
    private val entityPackets: MutableMap<Long, EntityPacket> = mutableMapOf()
    private val guilds: MutableMap<Long, GuildCreatePacket> = mutableMapOf()
    private val channels: MutableMap<Long, ChannelPacket> = mutableMapOf()

    fun <T : EntityPacket> cache(packet: T): T = packet.also {
        when (packet) {
            is GuildCreatePacket -> guilds[packet.id] = packet
            is ChannelPacket -> channels[packet.id] = packet
            else -> entityPackets[packet.id] = packet
        }
    }

    fun <T : EntityPacket> findId(id: Long): T? = entityPackets[id]?.let {
        @Suppress("UNCHECKED_CAST")
        entityPackets[id] as? T
    }

    inline fun <reified T : EntityPacket> find(filter: (T) -> Boolean): T? =
        filterIsInstance<T>().find(filter)

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : EntityPacket> filterIsInstance(): List<T> = when (T::class) {
        GuildCreatePacket::class -> guilds.values
        ChannelPacket::class -> channels.values
        else -> entityPackets.values.filterIsInstance<T>()
    }.toList() as List<T>
}

internal fun <T : EntityPacket> T.cache(): T = EntityPacketCache.cache(this)

internal fun <T : EntityPacket, C : Collection<T>> C.cacheAll() = onEach { it.cache() }
