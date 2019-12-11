package com.serebit.strife

import com.serebit.logkat.Logger
import com.serebit.logkat.info
import com.serebit.logkat.trace
import com.serebit.logkat.warn
import com.serebit.strife.data.Activity
import com.serebit.strife.data.AvatarData
import com.serebit.strife.data.OnlineStatus
import com.serebit.strife.data.Presence
import com.serebit.strife.entities.*
import com.serebit.strife.entities.User.Companion.USERNAME_LENGTH_RANGE
import com.serebit.strife.entities.User.Companion.USERNAME_MAX_LENGTH
import com.serebit.strife.entities.User.Companion.USERNAME_MIN_LENGTH
import com.serebit.strife.events.Event
import com.serebit.strife.internal.StatusUpdatePayload
import com.serebit.strife.internal.dispatches.DispatchConversionResult
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.network.Requester
import com.serebit.strife.internal.network.Route
import com.serebit.strife.internal.network.buildGateway
import com.serebit.strife.internal.packets.ActivityPacket
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GuildChannelPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.launch

/**
 * The [BotClient] represents a connection to the Discord API. Multiple instances of the same bot can connect
 * simultaneously, therefore each [BotClient] holds information relevant to each specific instance of the bot.
 * For example, getting the [selfUser] from BotClient_A may return a [User] with different information than
 * BotClient_B's [selfUser].
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class BotClient internal constructor(
    uri: String,
    token: String,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger,
    private val eventBroadcaster: BroadcastChannel<Event>
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    private val gateway = buildGateway(uri, token, logger) {
        onDispatch { dispatch ->
            // Attempt to convert the dispatch to an Event
            val result = dispatch.asEvent(this@BotClient)
            val typeName = result.type.toString()
                .removePrefix("com.serebit.strife.events.")
                .removeSuffix(" (Kotlin reflection is not available)")

            when (result) {
                is DispatchConversionResult.Success<*> -> {
                    // Supply the relevant active listeners with the event
                    coroutineScope.launch {
                        eventBroadcaster.send(result.event)
                    }

                    logger.trace("Dispatched event with type $typeName.")
                }
                is DispatchConversionResult.Failure<*> -> {
                    logger.warn("Failed to process $typeName: ${result.message}")
                }
            }
        }
    }

    /** The [UserData.id] of the bot client. */
    internal var selfUserID: Long = 0
    internal val requester = Requester(token, logger)
    internal val cache = BotCache(this)

    /** The bot client's associated [User]. */
    val selfUser: User by lazy { cache.get(GetCacheData.User(selfUserID))!!.lazyEntity }

    /** The gateway connection latency in milliseconds. */
    val gatewayLatency: Long get() = gateway.latencyMilliseconds

    /** Attempts to open a connection to the Discord API. */
    suspend fun connect() {
        gateway.connect()
    }

    /** Close the connection to Discord. */
    suspend fun disconnect() {
        gateway.disconnect()
        coroutineScope.cancel()
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

    /** The bot client’s associated [ApplicationInfo]. */
    suspend fun fetchApplicationInfo(): ApplicationInfo? = requester.sendRequest(Route.GetApplicationInfo)
        .value
        ?.let { ApplicationInfo(it, this) }

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
