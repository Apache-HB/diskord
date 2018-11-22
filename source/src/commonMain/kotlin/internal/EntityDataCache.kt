package com.serebit.diskord.internal

import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.entitydata.UserData
import com.serebit.diskord.internal.entitydata.channels.ChannelData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.findById
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

internal class EntityDataCache {
    internal val guilds = mutableMapOf<Long, GuildData>()
    internal val dmChannels = mutableMapOf<Long, DmChannelData>()
    internal val users = mutableMapOf<Long, UserData>()

    inline fun <reified T : ChannelData> findChannel(id: Long): T? = runBlocking {
        val deferred = mutableListOf(async { dmChannels.values.asSequence().filterIsInstance<T>().findById(id) })
        deferred += guilds.values.asSequence().map(GuildData::allChannels).map {
            async { it.asSequence().filterIsInstance<T>().findById(id) }
        }
        deferred.awaitAll().filterNotNull().firstOrNull()
    }
}
