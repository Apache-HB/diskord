package com.serebit.strife.internal.entitydata

import com.serebit.strife.BotClient
import com.serebit.strife.entities.Entity
import com.serebit.strife.internal.packets.EntityPacket

/**
 * An Object used as an internal intermediate stage between an [EntityPacket] received from Discord and the end-user
 * facing [Entity]. [EntityData] is stored in cache and updated at regular intervals.
 *
 * @param U The [EntityPacket] type used to update the [EntityData] instance
 * @param E The [Entity] type generated for end-user facing instances
 */
internal interface EntityData<U : EntityPacket, E : Entity> {
    /** The SnowFlake ID of this entity. All entities have a unique ID */
    val id: Long
    val context: BotClient
    /** An instance of an [Entity] that backreferences to this data. */
    val lazyEntity: E

    /** Update the information held in this [EntityData] instance with a [entity packet][U]. */
    fun update(packet: U)
}

// Entity map manipulation extensions

internal fun <T : EntityData<*, *>> MutableMap<Long, T>.add(data: T) = put(data.id, data)

internal fun <T : EntityData<*, *>> MutableMap<Long, T>.addAll(elements: Iterable<T>) =
    putAll(elements.associateBy { it.id })

internal operator fun <T : EntityData<*, *>> MutableMap<Long, T>.plusAssign(data: T) = plusAssign(data.id to data)

internal operator fun <T : EntityData<*, *>> MutableMap<Long, T>.plusAssign(elements: Iterable<T>) =
    plusAssign(elements.associateBy { it.id })
