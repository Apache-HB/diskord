package com.serebit.strife.internal

import com.soywiz.klock.DateFormat

/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX */
internal val DateFormat.Companion.ISO get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]XX").withOptional()

internal inline fun <reified T : Any?> MutableList<T>.move(fromIndex: Int, toIndex: Int) =
    apply { add(toIndex, removeAt(fromIndex)) }

/** Apply the [mappin]g function to produce a new index from the current index and value. */
internal inline fun <reified T : Any?> List<T>.move(mapping: (Int, T) -> Int): List<T> =
    mapIndexed { index, t -> mapping(index, t) to t }.sortedBy { it.first }.map { it.second }

