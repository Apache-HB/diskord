package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.EntityData
import com.serebit.diskord.internal.packets.EntityPacket

internal interface EntityDataCache<T : EntityData> {
    fun put(data: T)

    fun update(packet: EntityPacket)

    fun remove(id: Long)

    operator fun get(id: Long): T?
}

internal fun <T : EntityData> EntityDataCache<T>.putAll(elements: Iterable<T>) = elements.forEach(::put)

internal operator fun <T : EntityData> EntityDataCache<T>.plusAssign(data: T) = put(data)

internal operator fun <T : EntityData> EntityDataCache<T>.plusAssign(elements: Iterable<T>) = putAll(elements)

internal fun EntityDataCache<out EntityData>.removeAll(ids: Iterable<Long>) = ids.forEach(::remove)

internal operator fun EntityDataCache<out EntityData>.minusAssign(id: Long) = remove(id)

internal operator fun EntityDataCache<out EntityData>.minusAssign(ids: Iterable<Long>) = removeAll(ids)
