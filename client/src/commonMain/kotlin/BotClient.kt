package com.serebit.strife

import com.serebit.strife.data.Activity
import com.serebit.strife.data.AvatarData
import com.serebit.strife.entities.*
import com.serebit.strife.entities.User.Companion.USERNAME_LENGTH_RANGE
import com.serebit.strife.entities.User.Companion.USERNAME_MAX_LENGTH
import com.serebit.strife.entities.User.Companion.USERNAME_MIN_LENGTH
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.StatusUpdatePayload
import com.serebit.strife.internal.dispatches.DispatchConversionResult
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.network.buildGateway
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.set
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            val result = dispatch.asEvent(this@BotClient)
            val typeName = result.type.toString()
                .removePrefix("com.serebit.strife.events.")
                .removeSuffix(" (Kotlin reflection is not available)")

            when (result) {
                is DispatchConversionResult.Success<*> -> {
                    // Supply the relevant listeners with the event
                    listeners
                        .filter { result.type == it.eventType }
                        .forEach { scope.launch { it(result.event) } }

                    logger.trace("Dispatched event with type $typeName.")
                }
                is DispatchConversionResult.Failure<*> -> {
                    logger.warn("Failed to process $typeName: ${result.message}")
                }
            }
        }
    }
    private val logger = sessionInfo.logger

    /** The [UserData.id] of the bot client. */
    internal var selfUserID: Long = 0
    internal val requester = Requester(sessionInfo)
    internal val cache = Cache()

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
     * Gets a user by their [id]. Returns a [User] if the id corresponds to a valid user in Discord, or null if it does
     * not. If the user isn't found in the cache, this function will attempt to pull from Discord's servers.
     */
    suspend fun getUser(id: Long): User? = obtainUserData(id)?.lazyEntity

    internal suspend fun obtainUserData(id: Long) = cache.getUserData(id)
        ?: requester.sendRequest(Route.GetUser(id)).value
            ?.let { cache.pullUserData(it) }

    /**
     * Gets a [GuildChannel] by its [id]. Returns a [GuildChannel] if the id corresponds to a valid channel in Discord
     * that is accessible by the client, or null if it does not. If the channel isn't found in the cache, this function
     * will attempt to pull from Discord's servers.
     */
    suspend fun getGuildChannel(id: Long): GuildChannel? = obtainGuildChannelData(id)?.lazyEntity

    /** Obtains [GuildChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainGuildChannelData(id: Long) = cache.getGuildChannelData(id)
        ?: requestChannel(id)
            ?.let { it as? GuildChannelPacket }
            ?.let { packet ->
                packet.guild_id
                    ?.let { cache.getGuildData(it) }
                    ?.let { cache.pullGuildChannelData(it, packet) }
            }

    /** Obtains [GuildTextChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainGuildTextChannelData(id: Long) = cache.getGuildTextChannelData(id)
        ?: requestChannel(id)
            ?.let { it as? GuildTextChannelPacket }
            ?.let { packet ->
                packet.guild_id
                    ?.let { cache.getGuildData(it) }
                    ?.let { cache.pullGuildChannelData(it, packet) as GuildTextChannelData }
            }

    /**
     * Gets a [DmChannel] by its [id]. Returns a [DmChannel] if the id corresponds to a valid channel in Discord that
     * is accessible by the client, or null if it does not. If the channel isn't found in the cache, this function will
     * attempt to pull from Discord's servers.
     */
    suspend fun getDmChannel(id: Long): DmChannel? = obtainDmChannelData(id)?.lazyEntity

    /** Obtains [DmChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainDmChannelData(id: Long) = cache.getDmChannelData(id)
        ?: requestChannel(id)
            ?.let { it as? DmChannelPacket }
            ?.let { cache.pullDmChannelData(it) }

    /** Requests a [Channel] by its [id] from Discord servers. */
    internal suspend fun requestChannel(id: Long) = requester.sendRequest(Route.GetChannel(id)).value

    /**
     * Gets a guild by its [id]. Returns a [Guild] if the id corresponds to a guild that is accessible by the client,
     * or null if it does not. This function will only retrieve from the cache, as Discord does not send full guild
     * information via the HTTP endpoints. If the [Guild] is not available yet, this function will suspend and wait for
     * the guild's information.
     */
    suspend fun getGuild(id: Long): Guild? = cache.getGuildData(id)?.lazyEntity

    /**
     * Gets a [GuildRole] by its [id]. Returns a [GuildRole] if the id corresponds to a valid [GuildRole] that is
     * accessible by the client, or `null` if it does not. This function will only retrieve from the cache, as roles
     * are permanently stored by Strife.
     */
    fun getRole(id: Long): GuildRole? = cache.getRoleData(id)?.lazyEntity

    /**
     * Gets a [GuildEmoji] by its [id]. Returns a [GuildEmoji] if the id corresponds to a valid [GuildEmoji] that is
     * accessible by the client, or `null` if it does not. This function will only retrieve from the cache, as emojis
     * are permanently stored by Strife.
     */
    fun getEmoji(id: Long): GuildEmoji? = cache.getEmojiData(id)?.lazyEntity

    /**
     * An encapsulating class for caching [EntityData] using [LruWeakCache]. The [Cache] class contains functions for
     * retrieving and updating cached data.
     *
     * The functions of the [Cache] are named in a fashion mirroring `git` nomenclature.
     *
     *      To get a value from cache, with possibly null values
     *          getXData(id)
     *      To update OR add a value in cache with a packet
     *          pullXData(packet)
     *      To add a value to cache with a packet
     *          pushXData(packet)
     *      To remove a value from cache
     *          removeXData(id)
     */
    internal inner class Cache {
        private val guilds = HashMap<Long, CompletableDeferred<GuildData>>()
        private val roles = HashMap<Long, GuildRoleData>()
        private val emojis = HashMap<Long, GuildEmojiData>()
        private val guildChannels = HashMap<Long, GuildChannelData<*, *>>()
        private val dmChannels = LruWeakCache<Long, DmChannelData>()
        private val users = LruWeakCache<Long, UserData>()

        /** Get [UserData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getUserData(id: Long) = users[id]

        /**
         * Update & Get [UserData] from cache using a [UserPacket]. If there is no corresponding [UserData] in cache,
         * an instance will be created from the [packet] and added.
         */
        fun pullUserData(packet: UserPacket) = users[packet.id]?.apply { update(packet) }
            ?: packet.toData(this@BotClient).also { users[it.id] = it }

        /**
         * Get [GuildData] from *cache* and wait for it to be available. Will return `null` if the corresponding data
         * is not cached.
         */
        suspend fun getGuildData(id: Long) = guilds[id]?.await()

        /** Update & Get [GuildData] from cache using a [GuildUpdatePacket]. */
        @UseExperimental(ExperimentalCoroutinesApi::class)
        suspend fun pullGuildData(packet: GuildUpdatePacket) =
            guilds[packet.id]?.await()?.apply { update(packet) }

        /**
         * Use a [GuildCreatePacket] to add a new [GuildData] instance to cache and
         * [pull user data][Cache.pullUserData].
         */
        fun pushGuildData(packet: GuildCreatePacket) =
            packet.toData(this@BotClient)
                .also { guilds[it.id]?.complete(it) ?: guilds.put(it.id, CompletableDeferred(it)) }

        /** Initiate a [GuildData]. Used if we receive the guild's [id] in [Ready] dispatch. */
        fun initGuildData(id: Long) {
            guilds[id] = CompletableDeferred()
        }

        /**
         * Remove [GuildData] from the cache by its [id]. Will return the removed [GuildData], or `null` if the
         * corresponding data is not cached.
         */
        fun removeGuildData(id: Long) = guilds.remove(id)

        /** Get [GuildChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getGuildChannelData(id: Long) = guildChannels[id]

        /** Get [TextChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getGuildTextChannelData(id: Long) = guildChannels[id] as? TextChannelData<*, *>

        /** Get [GuildVoiceChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getGuildVoiceChannelData(id: Long) = guildChannels[id] as? GuildVoiceChannelData

        /** Update & Get [GuildChannelData] from cache using a [GuildChannelPacket]. */
        @Suppress("UNCHECKED_CAST")
        fun <P : GuildChannelPacket> pullGuildChannelData(guildData: GuildData, packet: P) =
            guildChannels[packet.id]?.let { it as GuildChannelData<P, *> }?.apply { update(packet) }
                ?: packet.toGuildChannelData(guildData, this@BotClient).also { guildChannels[packet.id] = it }

        /**
         * Remove [GuildChannelData] from the cache by its [id]. Will return the removed [ChannelData], or `null` if
         * the corresponding data is not cached.
         */
        fun removeGuildChannelData(id: Long) = guildChannels.remove(id)

        /** Get [DmChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getDmChannelData(id: Long) = dmChannels[id]

        /** Update & Get [DmChannelData] from cache using a [DmChannelPacket]. */
        fun pullDmChannelData(packet: DmChannelPacket) =
            dmChannels[packet.id]?.apply { update(packet) }
                ?: packet.toDmChannelData(this@BotClient).also { dmChannels[packet.id] = it }

        /**
         * Remove [DmChannelData] from the cache by its [id]. Will return the removed [DmChannelData], or `null` if the
         * corresponding data is not cached.
         */
        fun removeDmChannelData(id: Long) = dmChannels.remove(id)

        /** Get [GuildRoleData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getRoleData(id: Long) = roles[id]

        /**
         * Update & Get [GuildRoleData] from cache using a [GuildRolePacket]. If there is no corresponding
         * [GuildRoleData] in cache, an instance will be created from the [packet] and added.
         */
        fun pullRoleData(packet: GuildRolePacket) = roles[packet.id]?.apply { update(packet) }
            ?: packet.toData(this@BotClient).also { roles[packet.id] = it }

        /**
         * Remove [GuildRoleData] from the cache by its [id]. Will return the removed [GuildRoleData], or `null` if the
         * corresponding data is not cached.
         */
        fun removeRoleData(id: Long) = roles.remove(id)

        /** Get [GuildEmojiData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getEmojiData(id: Long) = emojis[id]

        /**
         * Update & Get [GuildEmojiData] from cache using a [GuildEmojiPacket]. If there is no corresponding
         * [GuildEmojiData] in cache, an instance will be created from the [packet] and added.
         */
        fun pullEmojiData(guildData: GuildData, packet: GuildEmojiPacket) = emojis[packet.id]?.apply { update(packet) }
            ?: packet.toData(guildData, this@BotClient).also { emojis[packet.id] = it }

        /**
         * Remove [GuildEmojiData] from the cache by its [id]. Will return the removed [GuildEmojiData], or `null` if
         * the corresponding data is not cached.
         */
        fun removeEmojiData(id: Long) = guilds.remove(id)
    }
}
