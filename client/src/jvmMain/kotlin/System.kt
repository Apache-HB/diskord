package com.serebit.strife.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.concurrent.thread

internal actual val osName: String get() = System.getProperty("os.name")

internal actual fun onProcessExit(callback: suspend CoroutineScope.() -> Unit) =
    Runtime.getRuntime().addShutdownHook(thread(false) {
        runBlocking(block = callback)
    })

internal actual val Throwable.stackTraceAsString: String
    get() = StringWriter().also { PrintWriter(it).use(::printStackTrace) }.toString()
