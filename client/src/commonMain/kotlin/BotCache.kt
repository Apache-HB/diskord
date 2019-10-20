package com.serebit.strife

import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.minusAssign
import com.serebit.strife.internal.packets.*
import com.serebit.strife.internal.set
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * An encapsulating class for caching [EntityData] using [LruWeakCache]. The [BotCache] class contains functions for
 * retrieving and updating cached data.
 *
 * The functions of the [BotCache] are named in a fashion mirroring `git` nomenclature.
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
internal class BotCache(private val client: BotClient) {
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
        ?: packet.toData(client).also { users[it.id] = it }

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
     * [pull user data][BotCache.pullUserData].
     */
    fun pushGuildData(packet: GuildCreatePacket) =
        packet.toData(client)
            .also { guilds[it.id]?.complete(it) ?: guilds.put(it.id, CompletableDeferred(it)) }

    /** Initiate a [GuildData]. Used if we receive the guild's [id] in [Ready] dispatch. */
    fun initGuildData(id: Long) {
        guilds[id] = CompletableDeferred()
    }

    /** Update & Get [GuildChannelData] from cache using a [GuildChannelPacket]. */
    @Suppress("UNCHECKED_CAST")
    fun <P : GuildChannelPacket> pullGuildChannelData(guildData: GuildData, packet: P) =
        guildChannels[packet.id]?.let { it as GuildChannelData<P, *> }?.apply { update(packet) }
            ?: packet.toGuildChannelData(guildData, client).also { guildChannels[packet.id] = it }

    /** Update & Get [DmChannelData] from cache using a [DmChannelPacket]. */
    fun pullDmChannelData(packet: DmChannelPacket) =
        dmChannels[packet.id]?.apply { update(packet) }
            ?: packet.toDmChannelData(client).also { dmChannels[packet.id] = it }

    /**
     * Update & Get [GuildRoleData] from cache using a [GuildRolePacket]. If there is no corresponding
     * [GuildRoleData] in cache, an instance will be created from the [packet] and added.
     */
    fun pullRoleData(packet: GuildRolePacket) = roles[packet.id]?.apply { update(packet) }
        ?: packet.toData(client).also { roles[packet.id] = it }

    /**
     * Update & Get [GuildEmojiData] from cache using a [GuildEmojiPacket]. If there is no corresponding
     * [GuildEmojiData] in cache, an instance will be created from the [packet] and added.
     */
    fun pullEmojiData(guildData: GuildData, packet: GuildEmojiPacket) = emojis[packet.id]?.apply { update(packet) }
        ?: packet.toData(guildData, client).also { emojis[packet.id] = it }

}
