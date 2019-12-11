package com.serebit.strife.internal

import kotlinx.coroutines.CoroutineScope

/** The name of the operating system. */
internal expect val osName: String

/** A final action to when the program is closing. */
internal expect fun onProcessExit(callback: suspend CoroutineScope.() -> Unit)

internal expect val Throwable.stackTraceAsString: String
