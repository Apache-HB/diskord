package com.serebit.strife.internal

import com.soywiz.klock.DateFormat
import kotlinx.coroutines.CoroutineScope

internal val DateFormat.Companion.ISO_FORMAT get() = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

// runBlocking doesn't exist in the common coroutines library because of JS, so create it here for use in common
expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
