package com.serebit.strife

import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.LRUCache
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.UserData
import com.serebit.strife.internal.entitydata.channels.ChannelData
import com.serebit.strife.internal.entitydata.channels.DmChannelData
import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import com.serebit.strife.internal.entitydata.channels.TextChannelData
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.onProcessExit
import com.serebit.strife.internal.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

/**
 * TODO Context DOCS!!!!!!!!!!!!!!!!!
 */
class Context internal constructor(
    private val hello: HelloPayload,
    private val gateway: Gateway,
    sessionInfo: SessionInfo,
    private val listeners: Set<EventListener>
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val logger = sessionInfo.logger
    internal val requester = Requester(sessionInfo)

    internal val userCache = LRUCache<Long, UserData>()
    internal val dmCache = LRUCache<Long, DmChannelData>()
    internal val groupDmCache = LRUCache<Long, GroupDmChannelData>()
    internal val guildCache = LRUCache<Long, GuildData>()

    /** The bot client as a [User][com.serebit.strife.entities.User]. */
    val selfUser by lazy { userCache[selfUserId]!!.toUser() }

    /** Attempts to open a [Gateway] session with the Discord API. */
    fun connect() {
        launch {
            // Adds the lambda as a Socket listener
            gateway.onDispatch { dispatch ->
                if (dispatch !is Unknown) {
                    // Attempt to convert the dispatch to an Event
                    dispatch.asEvent(this@Context)?.let { event ->
                        // Supply the relevant listeners with the event
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

    /** Returns the [UserData] associated with the given [id][UserData.id] or `null`. */
    internal fun getUserData(id: Long) = userCache[id]

    /** Returns the [DmChannelData] of the given [id][DmChannelData.id] from cache or `null`. */
    internal fun getDmChannelData(id: Long) = dmCache[id]

    /** Returns the [GroupDmChannelData] of the given [id][GroupDmChannelData.id] from cache or `null`. */
    internal fun getGroupDmData(id: Long) = groupDmCache[id]

    /** Returns the [GuildChannelData] of the given [id][GuildChannelData.id] from cache or `null`. */
    internal fun getGuildChannelData(id: Long) =
        guildCache.image.map { it.value.allChannels }.filter { it.isNotEmpty() }
            .firstOrNull { it.containsKey(id) }?.get(id)

    /** Returns the [TextChannelData] of the given [id][TextChannelData.id] from cache or `null`. */
    internal fun getTextChannelData(id: Long) = getChannelDataAs<TextChannelData>(id)

    /**
     * Returns [ChannelData] from either the [DM-Channel][dmCache] or [guildCache] as the given [type][C]
     * @param id The [id][ChannelData.id] of the [ChannelData]
     */
    internal inline fun <reified C : ChannelData> getChannelDataAs(id: Long) = getChannelData(id) as? C

    /**
     * Returns the [ChannelData] from either the [DM-Channel][dmCache] or [guildCache]
     * associated with the given [id][ChannelData.id]
     */
    internal fun getChannelData(id: Long): ChannelData? = runBlocking {
        listOf(
            async { dmCache[id] },
            async { groupDmCache[id] },
            async { getGuildChannelData(id) }
        ).awaitAll().filterNotNull().firstOrNull()
    }

    companion object {
        /** The [UserData.id] of the bot client. */
        internal var selfUserId: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/strife"
        const val version = "0.0.0"
    }
}
