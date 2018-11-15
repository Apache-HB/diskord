package com.serebit.diskord.internal

internal expect val osName: String

internal expect fun exitProcess(code: Int): Nothing

internal expect fun onProcessExit(callback: () -> Unit)
