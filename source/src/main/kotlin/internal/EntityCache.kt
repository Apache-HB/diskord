package com.serebit.diskord.internal

import com.serebit.diskord.entities.Entity
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal object EntityCache {
    private val cache: MutableMap<Long, Entity> = mutableMapOf()

    fun <T : Entity> cache(entity: T): T = entity.also {
        cache[entity.id] = entity
    }

    inline fun <reified T : Entity> find(id: Long): T? = find(T::class, id)

    fun <T : Entity> find(type: KClass<T>, id: Long): T? = cache[id]?.let {
        @Suppress("UNCHECKED_CAST")
        if (it::class == type || it::class.isSubclassOf(type)) cache[id] as? T else null
    }

    inline fun <reified T : Entity> filterIsInstance() = cache.values.filterIsInstance<T>()
}

internal inline fun <reified T : Entity> T.cache(): T = EntityCache.cache(this)
