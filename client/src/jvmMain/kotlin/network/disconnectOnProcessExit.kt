package com.serebit.strife.internal.network

import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

internal actual fun Gateway.disconnectOnProcessExit(connectionJob: Job) {
    Runtime.getRuntime().addShutdownHook(thread(false) {
        if (isConnected) runBlocking {
            disconnect()
            connectionJob.join()
        }
    })
}
