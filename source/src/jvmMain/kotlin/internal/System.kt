package com.serebit.diskord.internal

import kotlin.concurrent.thread
import kotlin.system.exitProcess

internal actual val osName: String get() = System.getProperty("os.name")

internal actual fun exitProcess(code: Int): Nothing = exitProcess(0)

internal actual fun onProcessExit(callback: () -> Unit) = Runtime.getRuntime().addShutdownHook(thread(false) { callback() } )
