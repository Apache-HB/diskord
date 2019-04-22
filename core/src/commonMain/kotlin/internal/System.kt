package com.serebit.strife.internal

/** The name of the operating system. */
internal expect val osName: String

/** A final action to when the program is closing. */
internal expect fun onProcessExit(callback: suspend () -> Unit)

internal expect val Throwable.stackTraceAsString: String
