package com.serebit.strife

import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.caching.DmChannelCache
import com.serebit.strife.internal.caching.GroupDmChannelCache
import com.serebit.strife.internal.caching.GuildCache
import com.serebit.strife.internal.caching.UserCache
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.channels.TextChannelData
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.onProcessExit
import com.serebit.strife.internal.runBlocking
import kotlinx.coroutines.*

class Context internal constructor(
    private val hello: HelloPayload,
    private val gateway: Gateway,
    sessionInfo: SessionInfo,
    private val listeners: Set<EventListener>
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val logger = sessionInfo.logger
    internal val guildCache = GuildCache()
    internal val userCache = UserCache()
    internal val dmChannelCache = DmChannelCache()
    internal val groupDmChannelCache = GroupDmChannelCache()
    internal val requester = Requester(sessionInfo)

    val selfUser by lazy { userCache[selfUserId]!!.toUser() }

    fun connect() {
        launch {
            gateway.onDispatch { dispatch ->
                if (dispatch !is Unknown) {
                    dispatch.asEvent(this@Context)?.let { event ->
                        listeners
                            .filter { it.eventType.isInstance(event) }
                            .forEach { launch { it(event) } }
                        logger.trace("Dispatched ${event::class.simpleName}.")
                    }
                } else logger.trace("Received unknown dispatch with type ${dispatch.t}")
            }
            logger.debug("Connected and received Hello payload. Opening session...")
            gateway.openSession(hello) {
                onProcessExit(::exit)
                logger.info("Opened a Discord session.")
            } ?: logger.error("Failed to open a new Discord session.")
        }
    }

    suspend fun exit() {
        gateway.disconnect()
        logger.info("Closed a Discord session.")
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
