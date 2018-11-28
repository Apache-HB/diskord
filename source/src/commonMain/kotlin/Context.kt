package com.serebit.diskord

import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.caching.DmChannelCache
import com.serebit.diskord.internal.caching.GroupDmChannelCache
import com.serebit.diskord.internal.caching.GuildCache
import com.serebit.diskord.internal.caching.UserCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class Context internal constructor(internal val requester: Requester, private inline val exitFunction: () -> Unit) {
    internal val guildCache = GuildCache()
    internal val userCache = UserCache()
    internal val dmChannelCache = DmChannelCache()
    internal val groupDmChannelCache = GroupDmChannelCache()
    val selfUser: User by lazy { User(selfUserId, this) }

    fun exit() = exitFunction()

    companion object {
        internal var selfUserId: Long = 0
    }
}

internal fun Context.findChannelInCaches(id: Long) = runBlocking {
    mutableListOf(
        async { dmChannelCache[id] },
        async { groupDmChannelCache[id] },
        async { guildCache.findChannel(id) }
    ).awaitAll().filterNotNull().firstOrNull()
}
