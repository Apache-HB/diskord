package com.serebit.diskord

import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.exitProcess
import com.serebit.diskord.internal.network.Gateway
import com.serebit.diskord.internal.network.SessionInfo
import com.serebit.diskord.internal.onProcessExit
import com.serebit.logkat.Logger

class Bot internal constructor(
    uri: String,
    sessionInfo: SessionInfo,
    listeners: Set<EventListener>,
    private val logger: Logger
) {
    private val gateway = Gateway(uri, sessionInfo, logger, listeners)

    init {
        logger.debug("Attempting to connect to Discord...")
        val hello = gateway.connect() ?: run {
            logger.fatal("Failed to connect to Discord.")
            exitProcess(0)
        }
        logger.debug("Connected and received Hello payload. Opening session...")
        gateway.openSession(hello)?.let {
            println("Connected to Discord.")
        }
        onProcessExit(::exit)
    }

    fun exit() {
        gateway.disconnect()
        println("Disconnected from Discord.")
    }

    companion object {
        const val sourceUri = "https://gitlab.com/serebit/diskord"
        const val version = "0.0.0"
    }
}
