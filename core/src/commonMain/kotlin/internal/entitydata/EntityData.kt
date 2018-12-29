package com.serebit.strife.internal.entitydata

import com.serebit.strife.Context

internal interface EntityData {
    val id: Long
    val context: Context
}

internal fun <T : EntityData> Iterable<T>.findById(id: Long) = find { it.id == id }

internal fun <T : EntityData> MutableIterable<T>.removeById(id: Long) = removeAll { it.id == id }
