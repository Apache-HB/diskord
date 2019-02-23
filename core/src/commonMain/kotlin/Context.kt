package com.serebit.strife

import com.serebit.strife.entities.GuildTextChannel
import com.serebit.strife.internal.EventListener
import com.serebit.strife.internal.HelloPayload
import com.serebit.strife.internal.LruCache
import com.serebit.strife.internal.dispatches.Unknown
import com.serebit.strife.internal.entitydata.ChannelData
import com.serebit.strife.internal.entitydata.GuildData
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
    internal val cache = Cache()

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

    internal inner class Cache(minSize: Int = DEFAULT_CACHE_MIN, maxSize: Int = DEFAULT_CACHE_MAX) {
        private val users = LruCache<Long, UserData>(minSize, maxSize)
        private val guilds = LruCache<Long, GuildData>(minSize, maxSize)
        private val channels = LruCache<Long, ChannelData<*, *>>(minSize, maxSize)

        fun getUserData(id: Long) = users[id]
        fun pullUserData(packet: UserPacket) = users[packet.id]?.also { it.update(packet) }
            ?: packet.toData(this@Context).also { users[it.id] = it }

        fun getGuildData(id: Long) = guilds[id]
        fun pullGuildData(packet: GuildUpdatePacket): GuildData = guilds[packet.id]!!.also { it.update(packet) }
        fun pushGuildData(packet: GuildCreatePacket): GuildData {
            packet.channels.forEach { }
            packet.members.forEach { pullUserData(it.user) }
            return packet.toData(this@Context).also { guilds[it.id] = it }
        }

        fun getChannelData(id: Long) = channels[id]
        inline fun <reified T : ChannelData<*, *>> getChannelDataAs(id: Long) = getChannelData(id) as? T
    }

    companion object {
        internal var selfUserID: Long = 0
        const val sourceUri = "https://gitlab.com/serebit/strife"
        const val version = "0.0.0"
        private const val DEFAULT_CACHE_MIN = 100
        private const val DEFAULT_CACHE_MAX = 10_000
    }
}
