package com.serebit.strife

import com.serebit.logkat.Logger
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.caching.DmChannelCache
import com.serebit.strife.internal.caching.GroupDmChannelCache
import com.serebit.strife.internal.caching.GuildCache
import com.serebit.strife.internal.caching.UserCache
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.channels.TextChannelData
import com.serebit.strife.internal.exitProcess
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.onProcessExit
import com.serebit.strife.internal.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class Context internal constructor(
    private val hello: HelloPayload,
    private val gateway: Gateway,
    internal val requester: Requester,
    private val listeners: Set<EventListener>,
    private val logger: Logger
) {
    internal val guildCache = GuildCache()
    internal val userCache = UserCache()
    internal val dmChannelCache = DmChannelCache()
    internal val groupDmChannelCache = GroupDmChannelCache()
    val selfUser by lazy { userCache[selfUserId]!!.toUser() }

    suspend fun connect() {
        gateway.onDispatch { dispatch ->
            if (dispatch !is Unknown) {
                dispatch.asEvent(this)?.let { event ->
                    listeners.asSequence()
                        .filter { it.eventType.isInstance(event) }
                        .forEach { it(event) }
                    logger.trace("Dispatched ${event::class.simpleName}.")
                }
            } else logger.trace("Received unknown dispatch with type ${dispatch.t}")
        }
        logger.debug("Connected and received Hello payload. Opening session...")
        gateway.openSession(hello) {
            println("Connected to Discord.")
            onProcessExit(::exit)
        } ?: run {
            logger.fatal("Failed to open a new Discord session.")
            exitProcess(1)
        }
    }

    suspend fun exit() {
        gateway.disconnect()
        println("Disconnected from Discord.")
    }

    companion object {
        internal var selfUserId: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/strife"
        const val version = "0.0.0"
    }
}

internal fun Context.findChannelInCaches(id: Long) = runBlocking {
    mutableListOf(
        async { dmChannelCache[id] },
        async { groupDmChannelCache[id] },
        async { guildCache.findChannel(id) }
    ).awaitAll().filterNotNull().firstOrNull()
}

internal fun Context.findTextChannelInCaches(id: Long) = findChannelInCaches(id) as? TextChannelData
