package com.serebit.strife.internal

import com.soywiz.klock.DateFormat
import kotlinx.coroutines.CoroutineScope

/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX */
internal val DateFormat.Companion.ISO_WITH_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXX")
/** [DateFormat] => yyyy-MM-dd'T'HH:mm:ssXX */
internal val DateFormat.Companion.ISO_WITHOUT_MS get() = DateFormat("yyyy-MM-dd'T'HH:mm:ssXX")

// runBlocking doesn't exist in the common coroutines library because of JS, so create it here for use in common
internal expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T

/** Remove and return the last entry of the [list][MutableList]. `null` if empty. */
internal fun <E> MutableList<E>.removeLastOrNull() = if (isEmpty()) null else removeAt(this.size - 1)
