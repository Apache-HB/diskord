package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context
import com.serebit.strife.entities.Entity
import com.serebit.strife.internal.packets.EntityPacket

/**
 * An Object used as an internal intermediate stage between an [EntityPacket] received from Discord and the end-user
 * facing [Entity]. [EntityData] is stored in cache and updated at regular intervals.
 */
internal interface EntityData {
    /** The SnowFlake ID of this entity. All entities have a unique ID */
    val id: Long
    val context: Context
}

internal fun <T : EntityData> Iterable<T>.findById(id: Long) = find { it.id == id }

internal fun <T : EntityData> MutableIterable<T>.removeById(id: Long) = removeAll { it.id == id }

internal fun <T : EntityData> MutableMap<Long, T>.add(data: T) = put(data.id, data)

internal fun <T : EntityData> MutableMap<Long, T>.addAll(elements: Iterable<T>) = putAll(elements.associateBy { it.id })

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(data: T) = plusAssign(data.id to data)

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(elements: Iterable<T>) =
    plusAssign(elements.associateBy { it.id })
