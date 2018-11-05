package com.serebit.diskord

import com.serebit.diskord.internal.EventDispatcher
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.Platform
import com.serebit.diskord.internal.network.Gateway
import com.serebit.logkat.Logger

class Diskord internal constructor(uri: String, token: String, listeners: Set<EventListener>) {
    private val eventDispatcher = EventDispatcher(listeners)
    private val gateway = Gateway(uri, token, eventDispatcher)

    init {
        Logger.debug("Attempting to connect to Discord...")
        val hello = gateway.connect() ?: run {
            Logger.fatal("Failed to connect to Discord.")
            Platform.exit(0)
        }
        Logger.debug("Connected and received Hello payload. Opening session...")
        gateway.openSession(hello)?.let {
            println("Connected to Discord.")
        }
        Platform.onExit(::exit)
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