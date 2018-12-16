package com.serebit.strife.internal.caching

import com.serebit.strife.internal.entitydata.EntityData

internal fun <T : EntityData> MutableMap<Long, T>.add(data: T) = put(data.id, data)

internal fun <T : EntityData> MutableMap<Long, T>.addAll(elements: Iterable<T>) = putAll(elements.associateBy { it.id })

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(data: T) = plusAssign(data.id to data)

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(elements: Iterable<T>) =
    plusAssign(elements.associateBy { it.id })
