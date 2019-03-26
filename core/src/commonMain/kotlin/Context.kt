package com.serebit.strife

import com.serebit.strife.entities.User
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.LruCache
import com.serebit.strife.internal.LruCache.Companion.DEFAULT_TRASH_SIZE
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Gateway
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.packets.*
import kotlinx.coroutines.launch

/**
 * The [Context] represents a connection to the Discord API. Multiple instances of the same bot can connect
 * simultaneously, therefore each [Context] holds information relevant to each specific instance of the bot.
 * For example, getting the [selfUser] from Context_A may return a [User] with different information than Context_B's
 * [selfUser].
 *
 * @param uri The URI used to build and connect this [Context]'s [Gateway].
 * @param sessionInfo The [SessionInfo] associated with this [Context].
 * @param listeners A set of [EventListener]s for this [Context]'s [Gateway] to use.
 */
class Context internal constructor(
    uri: String, sessionInfo: SessionInfo, private val listeners: Set<EventListener<*>>
) {
    private val gateway = Gateway(uri, sessionInfo)
    private val logger = sessionInfo.logger

    internal val requester = Requester(sessionInfo)
    internal val cache = Cache(trashSize = 50)

    /** The bot client as a [User][com.serebit.strife.entities.User]. */
    val selfUser by lazy { cache.getUserData(selfUserID)!!.toEntity() }

    /** Attempts to open a [Gateway] session with the Discord API. */
    suspend fun connect() {
        gateway.connect { scope, dispatch ->
            if (dispatch !is Unknown) {
                    // Attempt to convert the dispatch to an Event
                dispatch.asEvent(this@Context)?.let { event ->
                        // Supply the relevant listeners with the event
                    listeners
                        .filter { it.eventType.isInstance(event) }
                        .forEach { scope.launch { it(event) } }
                    logger.trace("Dispatched ${event::class.simpleName}.")
                } ?: logger.error("Failed to convert dispatch to event: \"${dispatch::class.simpleName}\"")
            } else logger.trace("Received unknown dispatch with type ${dispatch.t}")
        }
    }

    /** Close the [Gateway] session with discord. */
    suspend fun exit() {
        gateway.disconnect()
        logger.info("Closed a Discord session.")
    }

    /**
     * An encapsulating class for caching [com.serebit.strife.internal.entitydata.EntityData] using
     * [StrifeCaches][com.serebit.strife.internal.StrifeCache]. The [Cache] class contains functions
     * for retrieving and updating cached data.
     *
     * The functions of the [Cache] are named in a fashion mirroring `git` nomenclature.
     *
     *      To get a value from cache, with possibly null values
     *          getXData(id)
     *      To update OR add a value in cache with a packet
     *          pullXData(packet)
     *      To add a value to cache with a packet
     *          pushXData(packet)
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
         * Use a [GuildCreatePacket] to add a new [GuildData]
         * instance to cache and [pull user data][Cache.pullUserData].
         */
        fun pushGuildData(packet: GuildCreatePacket): GuildData {
            packet.members.forEach { pullUserData(it.user) }
            return packet.toData(this@Context).also { gd ->
                guilds[gd.id] = gd
                // The GuildCreate channels don't have IDs because ¯\_(ツ)_/¯
                packet.channels.forEach { cp -> pushChannelData(cp.toTypedPacket().apply { guild_id = gd.id }) }
            }
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
        fun <P : ChannelPacket> pullChannelData(packet: P) =
            (channels[packet.id] as? ChannelData<P, *>)?.also { it.update(packet) }
                ?: packet.toData(this@Context).also { channels[packet.id] = it }

        /** Remove an [EntityData][com.serebit.strife.internal.entitydata.EntityData] instance from the cache. */
        fun decache(id: Long) {
            when (id) {
                in channels -> {
                    val removed = channels.remove(id)
                    if (removed is GuildChannelData<*, *> && removed.guild.id in guilds) {
                        guilds[removed.guild.id]?.allChannels?.remove(removed.id)
                    }
                }
                in guilds -> guilds.remove(id)
                in users -> users.remove(id)
            }
        }
    }

    companion object {
        private const val DEFAULT_CACHE_MIN = 100
        private const val DEFAULT_CACHE_MAX = 10_000
        /** The [UserData.id] of the bot client. */
        internal var selfUserID: Long = 0
        /** The Strife GitLab page. */
        const val sourceUri = "https://gitlab.com/serebit/strife"
        /** The version of Strife this is running on. */
        const val version = "0.0.0"
    }
}
