package com.serebit.diskord.internal

import kotlinx.coroutines.CoroutineScope

// runBlocking doesn't exist in the common coroutines library because of JS, so create it here for use in common
expect fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T
