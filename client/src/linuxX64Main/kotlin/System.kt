package com.serebit.strife.internal

internal actual val osName: String get() = "linux-x64"

internal actual val Throwable.stackTraceAsString get() = getStackTrace().joinToString("\n")
