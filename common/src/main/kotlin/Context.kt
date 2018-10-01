package com.serebit.diskord

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Entity
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityCache
import kotlin.reflect.KClass

class Context internal constructor(val token: String, private val exitFunction: () -> Unit) {
    val selfUser: User
        get() = User.find(selfUserId) ?: throw EntityNotFoundException("No user with ID $selfUserId found.")

    inline fun <reified T : Entity> getEntityById(id: Long) = getEntityById(T::class, id)

    fun <T : Entity> getEntityById(type: KClass<T>, id: Long) = EntityCache.findId(type, id)

    fun exit() = exitFunction()

    companion object {
        internal var selfUserId: Long = 0
    }
}