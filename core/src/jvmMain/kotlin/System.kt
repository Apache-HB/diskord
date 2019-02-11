package com.serebit.strife.internal

import java.lang.Runtime.getRuntime
import kotlin.concurrent.thread

internal actual val osName: String get() = System.getProperty("os.name")

internal actual fun onProcessExit(callback: suspend () -> Unit) =
    getRuntime().addShutdownHook(thread(false) { runBlocking { callback() } })
