package com.serebit.strife.internal

internal expect val osName: String

internal expect fun exitProcess(code: Int): Nothing

internal expect fun onProcessExit(callback: suspend () -> Unit)
