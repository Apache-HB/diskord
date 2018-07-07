package com.serebit.diskord

import com.serebit.diskord.entities.User
import com.serebit.diskord.network.ApiEndpoint
import com.serebit.diskord.network.ApiRequester
import kotlinx.coroutines.experimental.runBlocking

class Context internal constructor(private val selfUserId: Long) {
    val selfUser: User
        get() = EntityCache.find(selfUserId)
            ?: runBlocking { ApiRequester.requestObject(ApiEndpoint.getUser(selfUserId)).await() }
            ?: throw IllegalArgumentException("Invalid self user ID $selfUserId passed to Context constructor.")
}
