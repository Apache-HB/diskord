package com.serebit.strife.internal

import com.soywiz.klock.DateFormat

/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX */
internal val DateFormat.Companion.ISO_WITH_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX")
/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ssXX */
internal val DateFormat.Companion.ISO_WITHOUT_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ssXX")

/* **************
    Collections
 ***************/

/** Remove and return the last entry of the [list][MutableList]. `null` if empty. */
internal fun <E> MutableList<E>.removeLastOrNull() = if (isEmpty()) null else removeAt(this.size - 1)

/** Remove any entries which meet the given [predicate]. Returns the removed entries in a [Map]. */
internal fun <K, V> MutableMap<K, V>.removeAll(predicate: (K, V) -> Boolean): Map<K, V> {
    val map = mutableMapOf<K, V>()
    for ((k, v) in this) if (predicate(k, v)) map[k] = remove(k)!!
    return map
}
