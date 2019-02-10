package com.serebit.strife

import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.*
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.UserData
import com.serebit.strife.internal.entitydata.channels.*
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import kotlinx.coroutines.*

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

    /**
     * @param id the [UserData.id]
     * @return the requested [UserData] from cache or null if not found
     */
    internal fun getUserData(id: Long) = userCache[id]

    /**
     * Attempt to locate [DmChannelData] from cache
     *
     * @param id the [DmChannelData.id]
     *
     * @return The specified [GuildChannelData] or `null`
     */
    internal fun getDmChannelData(id: Long) = dmCache[id]

    /**
     * Attempt to locate [GroupDmChannelData] from cache
     *
     * @param id the [GroupDmChannelData.id]
     *
     * @return The specified [GuildChannelData] or `null`
     */
    internal fun getGroupDmData(id: Long) = groupDmCache[id]

    /**
     * Attempt to locate [GuildChannelData] from a cache of [GuildData]
     *
     * @param id the [GuildChannelData.id]
     *
     * @return The specified [GuildChannelData] or `null`
     */
    internal fun getGuildChannelData(id: Long): GuildChannelData? =
        guildCache.image.map { it.value.allChannels }.filter { it.isNotEmpty() }
            .firstOrNull { it.containsKey(id) }?.get(id)

    /**
     * @param id the [TextChannelData.id]
     * @return [TextChannelData] by [id] from cache or null if not found
     */
    internal fun getTextChannelData(id: Long) =
        getChannelDataAs<TextChannelData>(id)

    /**
     * Get [ChannelData] from either the [DM-Channel][dmCache] or [guildCache]
     * as the given [type][C]
     *
     * @param id The [id][ChannelData.id] of the [ChannelData]
     *
     * @return The requested channel or null if not found
     */
    internal inline fun <reified C : ChannelData> getChannelDataAs(id: Long) =
        getChannelData(id) as? C

    /**
     * Get [ChannelData] from either the [DM-Channel][dmCache] or [guildCache]
     *
     * @param id The [id][ChannelData.id] of the [ChannelData]
     *
     * @return The requested channel or null if not found
     */
    internal fun getChannelData(id: Long): ChannelData? = runBlocking {
        mutableListOf(
            async { dmCache[id] },
            async { groupDmCache[id] },
            async { getGuildChannelData(id) }
        ).awaitAll().filterNotNull().firstOrNull()
    }

    companion object {
        internal var selfUserId: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/strife"
        const val version = "0.0.0"
    }
}
