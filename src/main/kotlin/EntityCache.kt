package com.serebit.diskord

import com.serebit.diskord.entities.Entity
import kotlin.reflect.KClass

internal object EntityCache {
    private val cache: MutableMap<Long, Entity> = mutableMapOf()

    fun <T : Entity> cache(entity: T): T {
        cache[entity.id] = entity
        return entity
    }

    inline fun <reified T : Entity> find(id: Long): T? = find(T::class, id)

    fun <T : Entity> find(type: KClass<T>, id: Long): T? = cache[id]?.let {
        @Suppress("UNCHECKED_CAST")
        if (it::class == type) cache[id] as? T else null
    }
}
