package com.serebit.diskord

import com.serebit.diskord.entities.Entity
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityCache

class Context internal constructor(val token: String, private val exitFunction: () -> Unit) {
    val selfUser: User by lazy { User(selfUserId) }

    fun <T : Entity> getEntityById(id: Long): T? = EntityCache.findId(id)

    fun exit() = exitFunction()

    companion object {
        internal var selfUserId: Long = 0
    }
}
