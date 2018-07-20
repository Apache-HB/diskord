package com.serebit.diskord

import com.serebit.diskord.entities.Entity

internal object EntityCache {
    private val cache: MutableMap<Long, Entity> = mutableMapOf()

    fun <T : Entity> cache(entity: T): T {
        cache[entity.id] = entity
        return entity
    }

    inline fun <reified T : Entity> find(id: Long): T? = cache[id]?.let {
        if (it is T) cache[id] as T else null
    }
}
