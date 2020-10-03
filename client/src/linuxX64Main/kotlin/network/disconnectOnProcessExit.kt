package com.serebit.strife.internal.network

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import platform.posix.on_exit

internal actual fun Gateway.disconnectOnProcessExit(connectionJob: Job) {
    on_exit(staticCFunction { _, ptr ->
        val (innerGateway, innerJob) = ptr?.asStableRef<Pair<Gateway, Job>>()?.get() ?: return@staticCFunction

        if (innerGateway.isConnected) runBlocking {
            innerGateway.disconnect()
            innerJob.join()
        }
    }, StableRef.create(this to connectionJob).asCPointer())
}
