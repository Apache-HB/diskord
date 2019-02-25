package com.serebit.strife

import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.LruCache
import com.serebit.strife.internal.LruCache.Companion.DEFAULT_TRASH_SIZE
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.ChannelData
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.GuildVoiceChannelData
import com.serebit.strife.internal.entitydata.TextChannelData
import com.serebit.strife.internal.entitydata.UserData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.onProcessExit
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.packets.GuildCreatePacket
import com.serebit.strife.internal.packets.GuildUpdatePacket
import com.serebit.strife.internal.packets.UserPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Context internal constructor(
    private val hello: HelloPayload,
    private val gateway: Gateway,
    sessionInfo: SessionInfo,
    private val listeners: Set<EventListener>
) : CoroutineScope {
    override val coroutineContext = Dispatchers.Default
    private val logger = sessionInfo.logger

    internal val requester = Requester(sessionInfo)
    internal val cache = Cache(trashSize = 50)

    val selfUser by lazy { cache.getUserData(selfUserID)!!.toEntity() }

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
     * An encapsulating class for caching [com.serebit.strife.internal.entitydata.EntityData] using
     * [StrifeCaches][com.serebit.strife.internal.StrifeCache]. The [Cache] class contains functions
     * for retrieving and updating cached data.
     *
     * @param maxSize The maximum size of each internal cache
     * @param minSize The minimum size any cache will self-reduce to
     * @param trashSize The number of entries to remove from cache while downsizing
     */
    internal inner class Cache(
        maxSize: Int = DEFAULT_CACHE_MAX,
        minSize: Int = DEFAULT_CACHE_MIN,
        trashSize: Int = DEFAULT_TRASH_SIZE
    ) {
        private val users = LruCache<Long, UserData>(minSize, maxSize, trashSize)
        private val guilds = LruCache<Long, GuildData>(minSize, maxSize, trashSize)
        private val channels = LruCache<Long, ChannelData<*, *>>(minSize, maxSize, trashSize)

        /** Get [UserData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getUserData(id: Long) = users[id]

        /**
         * Update & Get [UserData] from cache using a [UserPacket]. If there is no corresponding
         * [UserData] in cache, an instance will be created from the [packet] and added.
         */
        fun pullUserData(packet: UserPacket) = users[packet.id]?.also { it.update(packet) }
            ?: packet.toData(this@Context).also { users[it.id] = it }

        /** Get [GuildData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getGuildData(id: Long) = guilds[id]

        /** Update & Get [GuildData] from cache using a [GuildUpdatePacket]. */
        fun pullGuildData(packet: GuildUpdatePacket): GuildData = guilds[packet.id]!!.also { it.update(packet) }

        /**
         * Use a [GuildCreatePacket] to add a new [GuildData] instance to cache
         * and [pull user data][Cache.pullUserData].
         */
        fun pushGuildData(packet: GuildCreatePacket): GuildData {
            packet.channels.forEach { TODO() }
            packet.members.forEach { pullUserData(it.user) }
            return packet.toData(this@Context).also { guilds[it.id] = it }
        }

        /** Get [ChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getChannelData(id: Long) = channels[id]

        /** Get [ChannelData] as [T] from *cache*. Will return `null` if the corresponding data is not cached. */
        inline fun <reified T : ChannelData<*, *>> getChannelDataAs(id: Long) = getChannelData(id) as? T

        /** Get [TextChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getTextChannelData(id: Long): TextChannelData<*, *>? = getChannelDataAs(id)

        /** Get [GuildVoiceChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getVoiceChannelData(id: Long): GuildVoiceChannelData? = getChannelDataAs(id)

        /** Use a [ChannelPacket] to add a new [ChannelData] instance to cache. */
        fun pushChannelData(packet: ChannelPacket) = packet.toData(this@Context).also { channels[packet.id] = it }

        /** Update & Get [ChannelData] from cache using a [ChannelPacket]. */
        @Suppress("UNCHECKED_CAST")
        fun <P : ChannelPacket> pullChannelData(packet: P) {
            (channels[packet.id] as? ChannelData<P, *>)?.update(packet)
                ?: packet.toData(this@Context).also { channels[packet.id] = it }
        }

        /** Remove an [com.serebit.strife.internal.entitydata.EntityData] instance from cache. */
        fun decache(id: Long) = channels.remove(id) ?: guilds.remove(id) ?: users.remove(id)

    }

    companion object {
        internal var selfUserID: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/strife"
        const val version = "0.0.0"
        private const val DEFAULT_CACHE_MIN = 100
        private const val DEFAULT_CACHE_MAX = 10_000
    }
}
