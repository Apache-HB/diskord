package com.serebit.diskord.internal

import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.packets.GuildCreatePacket

internal object EntityCache {
    val entities: MutableMap<Long, Entity> = mutableMapOf()
    val guildCreatePackets = mutableMapOf<Long, GuildCreatePacket>()

    fun <T : Entity> cache(entity: T): T = entity.also { entities[entity.id] = entity }

    fun <T : Entity> findId(id: Long): T? = entities[id]?.let {
        @Suppress("UNCHECKED_CAST")
        entities[id] as? T
    }

    inline fun <reified T : Entity> find(filter: (T) -> Boolean): T? = asSequence().filterIsInstance<T>().find(filter)

    inline fun <reified T : Entity> filterIsInstance(): List<T> = entities.entries.filterIsInstance<T>()

    private fun asSequence() = Sequence { entities.iterator() }
}

internal fun <T : Entity> T.cache(): T = EntityCache.cache(this)
