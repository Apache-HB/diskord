package com.serebit.diskord.internal

import com.serebit.diskord.entities.Entity
import com.serebit.diskord.internal.packets.GuildCreatePacket
import kotlin.reflect.KClass

internal object EntityCache {
    private val entities: MutableMap<Long, Entity> = mutableMapOf()
    val guildCreatePackets = mutableMapOf<Long, GuildCreatePacket>()

    fun <T : Entity> cache(entity: T): T = entity.also { entities[entity.id] = entity }

    inline fun <reified T : Entity> findId(id: Long): T? = findId(T::class, id)

    @Suppress("UNUSED_PARAMETER")
    fun <T : Entity> findId(type: KClass<T>, id: Long): T? = entities[id]?.let {
        @Suppress("UNCHECKED_CAST")
        entities[id] as? T
    }

    inline fun <reified T : Entity> find(filter: (T) -> Boolean): T? = filterIsInstance<T>().find(filter)

    inline fun <reified T : Entity> filterIsInstance() = entities.values.filterIsInstance<T>()
}

internal inline fun <reified T : Entity> T.cache(): T = EntityCache.cache(this)
