package com.serebit.diskord

import com.serebit.diskord.entities.Entity
import com.serebit.diskord.entities.User
import com.serebit.diskord.network.Requester
import com.serebit.diskord.network.endpoints.GetUser
import kotlinx.coroutines.experimental.runBlocking
import kotlin.reflect.KClass

class Context internal constructor(val token: String) {
    val selfUser: User
        get() = EntityCache.find(selfUserId)
            ?: runBlocking { Requester.requestObject(GetUser(selfUserId)).await() }
            ?: throw IllegalArgumentException("Invalid self user ID $selfUserId passed to Context constructor.")

    inline fun <reified T : Entity> getEntityById(id: Long) = getEntityById(T::class, id)

    fun <T : Entity> getEntityById(type: KClass<T>, id: Long) = EntityCache.find(type, id)

    companion object {
        internal var selfUserId: Long = 0
    }
}
