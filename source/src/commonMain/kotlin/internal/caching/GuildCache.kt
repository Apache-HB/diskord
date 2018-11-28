package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.packets.EntityPacket
import com.serebit.diskord.internal.packets.GuildUpdatePacket
import com.serebit.diskord.internal.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class GuildCache : EntityDataCache<GuildData> {
    private val cache = mutableMapOf<Long, GuildData>()

    override fun put(data: GuildData) {
        cache[data.id] = data
    }

    override fun update(packet: EntityPacket) {
        cache[packet.id]?.update(packet as GuildUpdatePacket)
    }

    override fun remove(id: Long) {
        cache -= id
    }

    override fun get(id: Long): GuildData? = cache[id]

    fun findChannel(id: Long): GuildChannelData? = runBlocking {
        cache.values.map {
            async { it.allChannels[id] }
        }.awaitAll().filterNotNull().firstOrNull()
    }

    fun removeChannel(id: Long) {
        findChannel(id)?.guildId?.let { cache[it]?.allChannels?.remove(id) }
    }
}
