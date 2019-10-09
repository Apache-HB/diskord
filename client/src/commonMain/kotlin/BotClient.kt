package com.serebit.strife

import com.serebit.logkat.Logger
import com.serebit.strife.data.Activity
import com.serebit.strife.data.AvatarData
import com.serebit.strife.data.OnlineStatus
import com.serebit.strife.data.Presence
import com.serebit.strife.entities.*
import com.serebit.strife.entities.User.Companion.USERNAME_LENGTH_RANGE
import com.serebit.strife.entities.User.Companion.USERNAME_MAX_LENGTH
import com.serebit.strife.entities.User.Companion.USERNAME_MIN_LENGTH
import com.serebit.strife.internal.*
import com.serebit.strife.internal.EventListener.ListenerState
import com.serebit.strife.internal.dispatches.DispatchConversionResult
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.network.buildGateway
import com.serebit.strife.internal.packets.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * The [BotClient] represents a connection to the Discord API. Multiple instances of the same bot can connect
 * simultaneously, therefore each [BotClient] holds information relevant to each specific instance of the bot.
 * For example, getting the [selfUser] from BotClient_A may return a [User] with different information than
 * BotClient_B's [selfUser].
 *
 */
class BotClient internal constructor(
    uri: String,
    token: String,
    private val logger: Logger,
    createdListeners: Collection<EventListener<*>>
) {

    private val listeners = createdListeners.toMutableSet()
    private val gateway = buildGateway(uri, token, logger) {
        onDispatch { scope, dispatch ->
            // Attempt to convert the dispatch to an Event
            val result = dispatch.asEvent(this@BotClient)
            val typeName = result.type.toString()
                .removePrefix("com.serebit.strife.events.")
                .removeSuffix(" (Kotlin reflection is not available)")

            when (result) {
                is DispatchConversionResult.Success<*> -> {
                    // Supply the relevant active listeners with the event
                    listeners
                        .filter { result.type.isSubtypeOf(it.eventType) }
                        .filter { it.state == ListenerState.ACTIVE }
                        .forEach { scope.launch { it(result.event) } }

                    logger.trace("Dispatched event with type $typeName.")
                }
                is DispatchConversionResult.Failure<*> -> {
                    logger.warn("Failed to process $typeName: ${result.message}")
                }
            }

            // Remove terminated listeners
            if (listeners.removeAll { it.state == ListenerState.TERMINATED}) {
                logger.trace("Removed Terminated EventListeners.")
            }

        }
    }

    /** The [UserData.id] of the bot client. */
    internal var selfUserID: Long = 0
    internal val requester = Requester(token, logger)
    internal val cache = Cache()

    /** The bot client's associated [User]. */
    val selfUser: User by lazy { cache.get(GetCacheData.User(selfUserID))!!.lazyEntity }

    /** The gateway connection latency */
    val latency: Duration
        get() = gateway.latency

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
     * Update the [selfUser]'s current [Presence].
     *
     * Example:
     * ```
     * updatePresence(OnlineStatus.Online, Activity.Type.Playing to "!help")
     * ```
     *
     * @param onlineStatus The [selfUser]'s new [OnlineStatus].
     * @param activity The [selfUser]'s new [Activity], in the form of a [Pair] of [Activity.Type] mapped to the
     * [Activity]'s [name][Activity.name].
     */
    suspend fun updatePresence(onlineStatus: OnlineStatus, activity: Pair<Activity.Type, String>? = null) {
        gateway.updateStatus(
            StatusUpdatePayload(
                StatusUpdatePayload.Data(onlineStatus.name.toLowerCase(),
                    activity?.let { ActivityPacket(it.second, it.first.ordinal) })
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

    internal suspend fun obtainUserData(id: Long) = cache.get(GetCacheData.User(id))
        ?: requester.sendRequest(Route.GetUser(id)).value
            ?.let { cache.pullUserData(it) }

    /** Obtains [GuildChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainGuildChannelData(id: Long) = cache.get(GetCacheData.GuildChannel(id))
        ?: requester.sendRequest(Route.GetChannel(id)).value
            ?.let { it as? GuildChannelPacket }
            ?.let { packet ->
                packet.guild_id
                    ?.let { cache.getGuildData(it) }
                    ?.let { cache.pullGuildChannelData(it, packet) }
            }

    /** Obtains [GuildTextChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainGuildTextChannelData(id: Long) = cache.get(GetCacheData.GuildTextChannel(id))
        ?: requester.sendRequest(Route.GetChannel(id)).value
            ?.let { it as? GuildTextChannelPacket }
            ?.let { packet ->
                packet.guild_id
                    ?.let { cache.getGuildData(it) }
                    ?.let { cache.pullGuildChannelData(it, packet) as GuildTextChannelData }
            }

    /** Obtains [DmChannelData] from the cache, or the server if it's not available in the cache. */
    internal suspend fun obtainDmChannelData(id: Long) = cache.get(GetCacheData.DmChannel(id))
        ?: requester.sendRequest(Route.GetChannel(id)).value
            ?.let { it as? DmChannelPacket }
            ?.let { cache.pullDmChannelData(it) }

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

        inline fun <reified R> get(request: GetCacheData<R>): R? = when (request) {
            is GetCacheData.GuildChannel -> guildChannels[request.id] as? R
            is GetCacheData.GuildTextChannel -> guildChannels[request.id] as? R
            is GetCacheData.GuildVoiceChannel -> guildChannels[request.id] as? R
            is GetCacheData.GuildEmoji -> emojis[request.id] as? R
            is GetCacheData.GuildRole -> roles[request.id] as? R
            is GetCacheData.User -> users[request.id] as? R
            is GetCacheData.DmChannel -> dmChannels[request.id] as? R
        }

        inline fun <reified R> remove(request: RemoveCacheData<R>) = when (request) {
            is RemoveCacheData.Guild -> guilds.remove(request.id) as? R
            is RemoveCacheData.GuildChannel -> guildChannels.remove(request.id) as? R
            is RemoveCacheData.GuildEmoji -> emojis.remove(request.id) as? R
            is RemoveCacheData.GuildRole -> roles.remove(request.id) as? R
            is RemoveCacheData.User -> users.minusAssign(request.id) as? R
            is RemoveCacheData.DmChannel -> dmChannels.minusAssign(request.id) as? R
        }

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

        /** Update & Get [GuildChannelData] from cache using a [GuildChannelPacket]. */
        @Suppress("UNCHECKED_CAST")
        fun <P : GuildChannelPacket> pullGuildChannelData(guildData: GuildData, packet: P) =
            guildChannels[packet.id]?.let { it as GuildChannelData<P, *> }?.apply { update(packet) }
                ?: packet.toGuildChannelData(guildData, this@BotClient).also { guildChannels[packet.id] = it }

        /** Update & Get [DmChannelData] from cache using a [DmChannelPacket]. */
        fun pullDmChannelData(packet: DmChannelPacket) =
            dmChannels[packet.id]?.apply { update(packet) }
                ?: packet.toDmChannelData(this@BotClient).also { dmChannels[packet.id] = it }

        /**
         * Update & Get [GuildRoleData] from cache using a [GuildRolePacket]. If there is no corresponding
         * [GuildRoleData] in cache, an instance will be created from the [packet] and added.
         */
        fun pullRoleData(packet: GuildRolePacket) = roles[packet.id]?.apply { update(packet) }
            ?: packet.toData(this@BotClient).also { roles[packet.id] = it }

        /**
         * Update & Get [GuildEmojiData] from cache using a [GuildEmojiPacket]. If there is no corresponding
         * [GuildEmojiData] in cache, an instance will be created from the [packet] and added.
         */
        fun pullEmojiData(guildData: GuildData, packet: GuildEmojiPacket) = emojis[packet.id]?.apply { update(packet) }
            ?: packet.toData(guildData, this@BotClient).also { emojis[packet.id] = it }

    }
}

/**
 * Gets a [DmChannel] by its [id]. Returns a [DmChannel] if the id corresponds to a valid channel in Discord that
 * is accessible by the client, or null if it does not. If the channel isn't found in the cache, this function will
 * attempt to pull from Discord's servers.
 */
suspend fun BotClient.getDmChannel(id: Long): DmChannel? = obtainDmChannelData(id)?.lazyEntity

/**
 * Gets a [GuildChannel] by its [id]. Returns a [GuildChannel] if the id corresponds to a valid channel in Discord
 * that is accessible by the client, or null if it does not. If the channel isn't found in the cache, this function
 * will attempt to pull from Discord's servers.
 */
suspend fun BotClient.getGuildChannel(id: Long): GuildChannel? = obtainGuildChannelData(id)?.lazyEntity

/**
 * Gets a user by their [id]. Returns a [User] if the id corresponds to a valid user in Discord, or null if it does
 * not. If the user isn't found in the cache, this function will attempt to pull from Discord's servers.
 */
suspend fun BotClient.getUser(id: Long): User? = obtainUserData(id)?.lazyEntity

/**
 * Gets a guild by its [id]. Returns a [Guild] if the id corresponds to a guild that is accessible by the client,
 * or null if it does not. This function will only retrieve from the cache, as Discord does not send full guild
 * information via the HTTP endpoints. If the [Guild] is not available yet, this function will suspend and wait for
 * the guild's information.
 */
suspend fun BotClient.getGuild(id: Long): Guild? = cache.getGuildData(id)?.lazyEntity

/**
 * Gets a [GuildRole] by its [id]. Returns a [GuildRole] if the id corresponds to a valid [GuildRole] that is
 * accessible by the client, or `null` if it does not. This function will only retrieve from the cache, as roles
 * are permanently stored by Strife.
 */
fun BotClient.getRole(id: Long): GuildRole? = cache.get(GetCacheData.GuildRole(id))?.lazyEntity

/**
 * Gets a [GuildEmoji] by its [id]. Returns a [GuildEmoji] if the id corresponds to a valid [GuildEmoji] that is
 * accessible by the client, or `null` if it does not. This function will only retrieve from the cache, as emojis
 * are permanently stored by Strife.
 */
fun BotClient.getEmoji(id: Long): GuildEmoji? = cache.get(GetCacheData.GuildEmoji(id))?.lazyEntity

/**
 * Gets a [Webhook] by its [id]. Returns a [Webhook] if the id corresponds to a webhook accessible by the client, or
 * `null` if it does not.
 */
suspend fun BotClient.getWebhook(id: Long): Webhook? = requester.sendRequest(Route.GetWebhook(id)).value?.let {
    it.toEntity(
        this,
        cache.getGuildData(it.guild_id!!)!!,
        obtainGuildChannelData(it.channel_id) as GuildMessageChannelData<*, *>
    )
}

internal sealed class GetCacheData<T> {
    data class GuildEmoji(val id: Long) : GetCacheData<GuildEmojiData>()
    data class GuildRole(val id: Long) : GetCacheData<GuildRoleData>()
    data class GuildChannel(val id: Long) : GetCacheData<GuildChannelData<*, *>>()
    data class GuildTextChannel(val id: Long) : GetCacheData<GuildTextChannelData>()
    data class GuildVoiceChannel(val id: Long) : GetCacheData<GuildVoiceChannelData>()
    data class User(val id: Long) : GetCacheData<UserData>()
    data class DmChannel(val id: Long) : GetCacheData<DmChannelData>()
}

internal sealed class RemoveCacheData<T> {
    data class Guild(val id: Long) : RemoveCacheData<GuildData>()
    data class GuildEmoji(val id: Long) : RemoveCacheData<GuildEmojiData>()
    data class GuildRole(val id: Long) : RemoveCacheData<GuildRoleData>()
    data class GuildChannel(val id: Long) : RemoveCacheData<GuildChannelData<*, *>>()
    data class User(val id: Long) : RemoveCacheData<UserData>()
    data class DmChannel(val id: Long) : RemoveCacheData<DmChannelData>()
}
