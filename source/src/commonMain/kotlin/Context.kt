package com.serebit.diskord

import com.serebit.diskord.entities.toUser
import com.serebit.diskord.internal.EventListener
import com.serebit.diskord.internal.HelloPayload
import com.serebit.diskord.internal.caching.DmChannelCache
import com.serebit.diskord.internal.caching.GroupDmChannelCache
import com.serebit.diskord.internal.caching.GuildCache
import com.serebit.diskord.internal.caching.UserCache
import com.serebit.diskord.internal.dispatches.Unknown
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.exitProcess
import com.serebit.diskord.internal.network.Gateway
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.onProcessExit
import com.serebit.diskord.internal.runBlocking
import com.serebit.logkat.Logger
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
        internal var selfUserId: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/diskord"
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
