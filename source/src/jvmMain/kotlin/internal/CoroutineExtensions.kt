package com.serebit.strife.internal

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

actual fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T =
    kotlinx.coroutines.runBlocking(EmptyCoroutineContext, block)
