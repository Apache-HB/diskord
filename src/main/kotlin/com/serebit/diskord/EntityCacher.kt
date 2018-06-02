package com.serebit.diskord

import com.serebit.diskord.entities.DiscordEntity

internal object EntityCacher {
    private val cache: MutableMap<Long, DiscordEntity> = mutableMapOf()

    fun <T : DiscordEntity> cache(entity: T): T {
        cache[entity.id] = entity
        return entity
    }

    inline fun <reified T : DiscordEntity> find(id: Long): T? = cache[id]?.let {
        if (it is T) cache[id] as T else null
    }
}
