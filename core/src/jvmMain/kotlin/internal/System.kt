package com.serebit.strife.internal

import kotlin.concurrent.thread
import kotlin.system.exitProcess

internal actual val osName: String get() = System.getProperty("os.name")

internal actual fun exitProcess(code: Int): Nothing = exitProcess(code)

internal actual fun onProcessExit(callback: suspend () -> Unit) =
    Runtime.getRuntime().addShutdownHook(thread(false) {
        runBlocking { callback() }
    })
