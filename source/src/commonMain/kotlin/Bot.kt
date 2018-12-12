package com.serebit.diskord

import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.HelloPayload
import com.serebit.diskord.internal.exitProcess
import com.serebit.diskord.internal.network.Gateway
import com.serebit.diskord.internal.onProcessExit
import com.serebit.logkat.Logger

class Bot internal constructor(
    private val hello: HelloPayload,
    private val gateway: Gateway,
    listeners: Set<EventListener>,
    private val logger: Logger
) {
    suspend fun connect() {
        logger.debug("Connected and received Hello payload. Opening session...")
        gateway.openSession(hello) ?: run {
            logger.fatal("Failed to open a new Discord session.")
            exitProcess(1)
        }
        println("Connected to Discord.")
        onProcessExit(::exit)
    }

    suspend fun exit() {
        gateway.disconnect()
        println("Disconnected from Discord.")
    }

    companion object {
        const val sourceUri = "https://gitlab.com/serebit/diskord"
        const val version = "0.0.0"
    }
}
