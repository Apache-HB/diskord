package com.serebit.diskord.internal

import com.serebit.diskord.internal.packets.EntityPacket

internal object EntityPacketCache {
    private val entityPackets: MutableMap<Long, EntityPacket> = mutableMapOf()

    fun <T : EntityPacket> cache(packet: T): T = packet.also { entityPackets[packet.id] = packet }

    fun <T : EntityPacket> findId(id: Long): T? = entityPackets[id]?.let {
        @Suppress("UNCHECKED_CAST")
        entityPackets[id] as? T
    }

    inline fun <reified T : EntityPacket> find(filter: (T) -> Boolean): T? =
        asSequence().filterIsInstance<T>().find(filter)

    fun asSequence() = Sequence { entityPackets.iterator() }
}

internal fun <T : EntityPacket> T.cache(): T = EntityPacketCache.cache(this)

internal fun <T : EntityPacket, C : Collection<T>> C.cacheAll() = onEach { it.cache() }
