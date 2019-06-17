package com.serebit.strife

import com.serebit.strife.data.Activity
import com.serebit.strife.data.AvatarData
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.Guild
import com.serebit.strife.entities.User
import com.serebit.strife.entities.User.Companion.USERNAME_LENGTH_RANGE
import com.serebit.strife.entities.User.Companion.USERNAME_MAX_LENGTH
import com.serebit.strife.entities.User.Companion.USERNAME_MIN_LENGTH
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.LruCache
import com.serebit.strife.internal.LruCache.Companion.DEFAULT_TRASH_SIZE
import com.serebit.strife.internal.StatusUpdatePayload
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.network.buildGateway
import com.serebit.strife.internal.packets.*
import kotlinx.coroutines.launch

/**
 * The [BotClient] represents a connection to the Discord API. Multiple instances of the same bot can connect
 * simultaneously, therefore each [BotClient] holds information relevant to each specific instance of the bot.
 * For example, getting the [selfUser] from BotClient_A may return a [User] with different information than
 * BotClient_B's [selfUser].
 */
class BotClient internal constructor(
    uri: String, sessionInfo: SessionInfo, private val listeners: Set<EventListener<*>>
) {
    private val gateway = buildGateway(uri, sessionInfo) {
        onDispatch { scope, dispatch ->
            // Attempt to convert the dispatch to an Event
            dispatch.asEvent(this@BotClient)?.let { event ->
                // Supply the relevant listeners with the event
                listeners
                    .filter { it.eventType.isInstance(event) }
                    .forEach { scope.launch { it(event) } }
                logger.trace("Dispatched ${event::class.simpleName}.")
            } ?: logger.error("Failed to convert dispatch to event: \"${dispatch::class.simpleName}\"")
        }
    }
    private val logger = sessionInfo.logger

    /** The [UserData.id] of the bot client. */
    internal var selfUserID: Long = 0
    internal val requester = Requester(sessionInfo)
    internal val cache = Cache(trashSize = 50)

    /** The bot client's associated [User]. */
    val selfUser: User by lazy { cache.getUserData(selfUserID)!!.lazyEntity }

    /** Attempts to open a connection to the Discord API. */
    suspend fun connect() {
        gateway.connect()
    }

    /** Close the connection to Discord. */
    suspend fun disconnect() {
        gateway.disconnect()
        logger.info("Closed a Discord session.")
    }

    /**
     * Update the bot client's [OnlineStatus] and [Activity].
     *
     * @param status IDLE, DND, ONLINE, or OFFLINE
     * @param activity The new [Activity] (optional).
     */
    suspend fun updatePresence(status: OnlineStatus, activity: Activity? = null) {
        gateway.updateStatus(
            StatusUpdatePayload(
                StatusUpdatePayload.Data(status.name.toLowerCase(),
                    activity?.let { ActivityPacket(it.name, it.type.ordinal) })
            )
        ).also { logger.trace("Updated presence.") }
    }

    /**
     * Modifies the [selfUser]'s [User.username] and [User.avatar].
     * @see User.username for restrictions regarding [username].
     */
    suspend fun modifySelfUser(username: String? = null, avatarData: AvatarData? = null): User? {
        username?.also {
            require(it.length in USERNAME_LENGTH_RANGE) {
                "Username must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH"
            }
        }

        return requester
            .sendRequest(Route.ModifyCurrentUser(username, avatarData))
            .value
            ?.toData(this)
            ?.lazyEntity
    }

    /**
     * Gets a user by their [id]. Returns a [User] if the id corresponds to a valid user in Discord, or null if it
     * does not. If the user isn't found in the cache, this function will attempt to pull from Discord's servers.
     */
    suspend fun getUser(id: Long): User? = cache.getUserData(id)?.lazyEntity
        ?: requester.sendRequest(Route.GetUser(id)).value
            ?.let { cache.pullUserData(it) }
            ?.lazyEntity

    /**
     * Gets a channel by its [id]. Returns a [Channel] if the id corresponds to a valid channel in Discord that is
     * accessible by the client, or null if it does not. If the channel isn't found in the cache, this function will
     * attempt to pull from Discord's servers.
     */
    suspend fun getChannel(id: Long): Channel? = cache.getChannelData(id)?.lazyEntity
        ?: requester.sendRequest(Route.GetChannel(id)).value
            ?.toTypedPacket()
            ?.let { cache.pushChannelData(it) }
            ?.lazyEntity

    /**
     * Gets a guild by its [id]. Returns a [Guild] if the id corresponds to a guild that is accessible by the client,
     * or null if it does not. This function will only retrieve from the cache, as Discord does not send full guild
     * information via the HTTP endpoints.
     */
    fun getGuild(id: Long): Guild? = cache.getGuildData(id)?.lazyEntity

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
            ?: packet.toData(this@BotClient).also { users[it.id] = it }

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
            return packet.toData(this@BotClient).also { gd ->
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
        fun pushChannelData(packet: ChannelPacket) = packet.toData(this@BotClient).also { channels[packet.id] = it }

        /** Update & Get [ChannelData] from cache using a [ChannelPacket]. */
        @Suppress("UNCHECKED_CAST")
        fun <P : ChannelPacket> pullChannelData(packet: P) =
            (channels[packet.id] as? ChannelData<P, *>)?.also { it.update(packet) }
                ?: packet.toData(this@BotClient).also { channels[packet.id] = it }

        /** Remove an [EntityData] instance from the cache. */
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
    }
}
