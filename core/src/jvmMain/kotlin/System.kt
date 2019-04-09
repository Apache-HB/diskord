package com.serebit.strife.internal

import kotlinx.coroutines.runBlocking
import kotlinx.io.PrintWriter
import kotlinx.io.StringWriter
import kotlin.concurrent.thread

internal actual val osName: String get() = System.getProperty("os.name")

internal actual inline fun onProcessExit(crossinline callback: suspend () -> Unit) =
    Runtime.getRuntime().addShutdownHook(thread(false) {
        runBlocking { callback() }
    })

internal actual val Throwable.stackTraceAsString: String
    get() = StringWriter().let { stringWriter ->
        PrintWriter(stringWriter).use { printStackTrace(it) }
        stringWriter.toString()
    }
