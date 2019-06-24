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
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.network.SessionInfo
import com.serebit.strife.internal.network.buildGateway
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.set
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
            dispatch.asEvent(this@BotClient)?.let { (event, type) ->
                // Supply the relevant listeners with the event
                listeners
                    .filter { type == it.eventType }
                    .forEach { scope.launch { it(event) } }

                type.toString().removeSuffix(" (Kotlin reflection is not available)")
                    .let { logger.trace("Dispatched $it.") }
            } ?: logger.error("Failed to convert dispatch to event")
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
        private val users = LruWeakCache<Long, UserData>()
        private val guildChannels = HashMap<Long, GuildChannelData<*, *>>()
        private val dmChannels = LruWeakCache<Long, DmChannelData>()
        private val guilds = HashMap<Long, GuildData>()
        private val roles = HashMap<Long, GuildRoleData>()
        private val emojis = HashMap<Long, GuildEmojiData>()

        /** Get [UserData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getUserData(id: Long) = users[id]

        /**
         * Update & Get [UserData] from cache using a [UserPacket]. If there is no corresponding [UserData] in cache,
         * an instance will be created from the [packet] and added.
         */
        fun pullUserData(packet: UserPacket) = users[packet.id]?.apply { update(packet) }
            ?: packet.toData(this@BotClient).also { users[it.id] = it }

        /** Get [GuildData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getGuildData(id: Long) = guilds[id]

        /** Update & Get [GuildData] from cache using a [GuildUpdatePacket]. */
        fun pullGuildData(packet: GuildUpdatePacket) = guilds[packet.id]!!.apply { update(packet) }

        /**
         * Use a [GuildCreatePacket] to add a new [GuildData] instance to cache and
         * [pull user data][Cache.pullUserData].
         */
        fun pushGuildData(packet: GuildCreatePacket) =
            packet.toData(this@BotClient).also { guilds[it.id] = it }

        /**
         * Remove [GuildData] from the cache by its [id]. Will return the removed [GuildData], or `null` if the
         * corresponding data is not cached.
         */
        fun removeGuildData(id: Long) = guilds.remove(id)

        /** Get [ChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getChannelData(id: Long) = guildChannels[id]

        /** Get [ChannelData] as [T] from *cache*. Will return `null` if the corresponding data is not cached. */
        inline fun <reified T : ChannelData<*, *>> getChannelDataAs(id: Long) = getChannelData(id) as? T

        /** Get [TextChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getTextChannelData(id: Long): TextChannelData<*, *>? = getChannelDataAs(id)

        /** Get [GuildVoiceChannelData] from *cache*. Will return `null` if the corresponding data is not cached. */
        fun getVoiceChannelData(id: Long): GuildVoiceChannelData? = getChannelDataAs(id)

        /** Use a [ChannelPacket] to add a new [ChannelData] instance to cache. */
        fun pushChannelData(packet: ChannelPacket) =
            packet.toData(this@BotClient).also { guildChannels[packet.id] = it }

        /** Update & Get [ChannelData] from cache using a [ChannelPacket]. */
        @Suppress("UNCHECKED_CAST")
        fun <P : ChannelPacket> pullChannelData(packet: P) =
            (guildChannels[packet.id] as? ChannelData<P, *>)?.apply { update(packet) }
                ?: packet.toData(this@BotClient).also { guildChannels[packet.id] = it }

        /**
         * Remove [ChannelData] from the cache by its [id]. Will return the removed [ChannelData], or `null` if the
         * corresponding data is not cached.
         */
        fun removeChannelData(id: Long) = guildChannels.remove(id)

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
        fun pullEmojiData(packet: GuildEmojiPacket) = emojis[packet.id]?.apply { update(packet) }
            ?: packet.toData(this@BotClient).also { emojis[packet.id] = it }

        /**
         * Remove [GuildEmojiData] from the cache by its [id]. Will return the removed [GuildEmojiData], or `null` if
         * the corresponding data is not cached.
         */
        fun removeEmojiData(id: Long) = guilds.remove(id)
    }
}
