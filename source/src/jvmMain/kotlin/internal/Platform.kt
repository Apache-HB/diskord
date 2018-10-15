package com.serebit.diskord.internal

import kotlin.concurrent.thread
import kotlin.system.exitProcess

actual object Platform {
    actual fun exit(code: Int): Nothing = exitProcess(0)

    actual fun onExit(callback: () -> Unit) = Runtime.getRuntime().addShutdownHook(thread(false) { callback() } )
}
