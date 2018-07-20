package com.serebit.diskord

import com.serebit.diskord.entities.User
import com.serebit.diskord.network.ApiRequester
import com.serebit.diskord.network.endpoints.GetUser
import kotlinx.coroutines.experimental.runBlocking

class Context internal constructor(private val selfUserId: Long) {
    val selfUser: User
        get() = EntityCache.find(selfUserId)
            ?: runBlocking { ApiRequester.requestObject(GetUser(selfUserId)).await() }
            ?: throw IllegalArgumentException("Invalid self user ID $selfUserId passed to Context constructor.")
}
