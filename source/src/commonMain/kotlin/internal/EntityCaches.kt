package com.serebit.strife.internal.caching

import com.serebit.strife.internal.entitydata.EntityData
import com.serebit.strife.internal.entitydata.GuildData
import com.serebit.strife.internal.entitydata.UserData
import com.serebit.strife.internal.entitydata.channels.DmChannelData
import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class DmChannelCache : MutableMap<Long, DmChannelData> by mutableMapOf()

internal class GroupDmChannelCache : MutableMap<Long, GroupDmChannelData> by mutableMapOf()

internal class GuildCache : MutableMap<Long, GuildData> by mutableMapOf() {
    suspend fun findChannel(id: Long): GuildChannelData? = coroutineScope {
        values.map { async { it.allChannels[id] } }.awaitAll().filterNotNull().firstOrNull()
    }

    suspend fun removeChannel(id: Long) {
        findChannel(id)?.guild?.allChannels?.remove(id)
    }
}

internal class UserCache : MutableMap<Long, UserData> by mutableMapOf()

internal fun <T : EntityData> MutableMap<Long, T>.add(data: T) = put(data.id, data)

internal fun <T : EntityData> MutableMap<Long, T>.addAll(elements: Iterable<T>) = putAll(elements.associateBy { it.id })

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(data: T) = plusAssign(data.id to data)

internal operator fun <T : EntityData> MutableMap<Long, T>.plusAssign(elements: Iterable<T>) =
    plusAssign(elements.associateBy { it.id })
