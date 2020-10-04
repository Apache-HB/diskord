package com.serebit.strife

import com.serebit.strife.internal.LruWeakCache
import com.serebit.strife.internal.dispatches.Ready
import com.serebit.strife.internal.entitydata.*
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
    private val guilds = mutableMapOf<Long, CompletableDeferred<GuildData>>()
    private val roles = mutableMapOf<Long, GuildRoleData>()
    private val emojis = mutableMapOf<Long, GuildEmojiData>()
    private val guildChannels = mutableMapOf<Long, GuildChannelData<*, *>>()
    private val dmChannels = LruWeakCache<Long, DmChannelData>()
    private val messages = mutableMapOf<Long, LruWeakCache<Long, MessageData>>()
    private val users = LruWeakCache<Long, UserData>()

    inline fun <reified R> get(request: GetCacheData<R>): R? = when (request) {
        is GetCacheData.GuildChannel -> guildChannels[request.id]
        is GetCacheData.GuildTextChannel -> guildChannels[request.id]
        is GetCacheData.GuildVoiceChannel -> guildChannels[request.id]
        is GetCacheData.GuildEmoji -> emojis[request.id]
        is GetCacheData.GuildRole -> roles[request.id]
        is GetCacheData.User -> users[request.id]
        is GetCacheData.DmChannel -> dmChannels[request.id]
        is GetCacheData.Message -> messages[request.channelID]?.get(request.id)
        is GetCacheData.LatestMessage -> messages[request.channelID]?.values?.maxByOrNull { it.createdAt }
    } as? R

    fun <R> remove(request: RemoveCacheData<R>) {
        when (request) {
            is RemoveCacheData.Guild -> guilds.remove(request.id)
            is RemoveCacheData.GuildChannel -> guildChannels.remove(request.id)
            is RemoveCacheData.GuildEmoji -> emojis.remove(request.id)
            is RemoveCacheData.GuildRole -> roles.remove(request.id)
            is RemoveCacheData.User -> users.remove(request.id)
            is RemoveCacheData.DmChannel -> dmChannels.remove(request.id)
            is RemoveCacheData.Message -> messages[request.channelID]?.remove(request.id)
        }
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
    @OptIn(ExperimentalCoroutinesApi::class)
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

    fun pushMessageData(packet: MessageCreatePacket): MessageData = packet.toData(client).also {
        messages.getOrPut(packet.channel_id) { LruWeakCache() }.put(packet.id, it)
    }
}

internal sealed class GetCacheData<T> {
    data class GuildEmoji(val id: Long) : GetCacheData<GuildEmojiData>()
    data class GuildRole(val id: Long) : GetCacheData<GuildRoleData>()
    data class GuildChannel(val id: Long) : GetCacheData<GuildChannelData<*, *>>()
    data class GuildTextChannel(val id: Long) : GetCacheData<GuildTextChannelData>()
    data class GuildVoiceChannel(val id: Long) : GetCacheData<GuildVoiceChannelData>()
    data class User(val id: Long) : GetCacheData<UserData>()
    data class DmChannel(val id: Long) : GetCacheData<DmChannelData>()
    data class Message(val id: Long, val channelID: Long) : GetCacheData<MessageData>()
    data class LatestMessage(val channelID: Long) : GetCacheData<MessageData?>()
}

internal sealed class RemoveCacheData<T> {
    data class Guild(val id: Long) : RemoveCacheData<GuildData>()
    data class GuildEmoji(val id: Long) : RemoveCacheData<GuildEmojiData>()
    data class GuildRole(val id: Long) : RemoveCacheData<GuildRoleData>()
    data class GuildChannel(val id: Long) : RemoveCacheData<GuildChannelData<*, *>>()
    data class User(val id: Long) : RemoveCacheData<UserData>()
    data class DmChannel(val id: Long) : RemoveCacheData<DmChannelData>()
    data class Message(val id: Long, val channelID: Long) : RemoveCacheData<MessageData>()
}
