package com.serebit.diskord.internal.entitydata

import com.serebit.diskord.Context

internal interface EntityData {
    val id: Long
    val context: Context
}

internal fun <T : EntityData> Iterable<T>.findById(id: Long) = find { it.id == id }

internal fun <T : EntityData> MutableIterable<T>.removeById(id: Long) = removeAll { it.id == id }
