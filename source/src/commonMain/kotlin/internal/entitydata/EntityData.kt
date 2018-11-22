package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context

internal interface EntityData {
    val id: Long
    val context: Context
}

internal fun <T : EntityData, C : Iterable<T>> C.findById(id: Long) = find { it.id == id }

internal fun <T : EntityData, C : Sequence<T>> C.findById(id: Long) = find { it.id == id }

internal fun <T : EntityData, C : MutableIterable<T>> C.removeById(id: Long) = removeAll { it.id == id }
