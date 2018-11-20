package com.serebit.diskord

import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.network.Requester

class Context internal constructor(
    internal val requester: Requester,
    private inline val exitFunction: () -> Unit
) {
    val selfUser: User by lazy { User(selfUserId, this) }

    fun exit() = exitFunction()

    companion object {
        internal var selfUserId: Long = 0
    }
}
