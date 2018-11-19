package com.serebit.diskord.internal

import com.serebit.diskord.internal.packets.EntityPacket

internal object EntityPacketCache {
    private val entityPackets = mutableListOf<EntityPacket>()

    inline fun <reified T : EntityPacket> cache(packet: T): T {
        entityPackets.removeAll { it is T && it.id == packet.id }
        entityPackets.add(packet)
        return packet
    }

    inline fun <reified T : EntityPacket> findId(id: Long): T? = entityPackets.find { it is T && it.id == id } as? T

    inline fun <reified T : EntityPacket> find(filter: (T) -> Boolean): T? =
        filterIsInstance<T>().find(filter)

    inline fun <reified T : EntityPacket> filterIsInstance(): List<T> = entityPackets.filterIsInstance<T>()
}

internal inline fun <reified T : EntityPacket> T.cache(): T = EntityPacketCache.cache(this)

internal inline fun <reified T : EntityPacket, C : Collection<T>> C.cacheAll() = onEach { it.cache() }
