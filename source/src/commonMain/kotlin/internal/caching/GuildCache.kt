package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class GuildCache : MutableMap<Long, GuildData> by mutableMapOf() {
    fun findChannel(id: Long): GuildChannelData? = runBlocking {
        values.map {
            async { it.allChannels[id] }
        }.awaitAll().filterNotNull().firstOrNull()
    }

    fun removeChannel(id: Long) {
        findChannel(id)?.guildId?.let { get(it)?.allChannels?.remove(id) }
    }
}
