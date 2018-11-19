package com.serebit.diskord

import com.serebit.diskord.entities.User

class Context internal constructor(val token: String, private inline val exitFunction: () -> Unit) {
    val selfUser: User by lazy { User(selfUserId) }

    fun exit() = exitFunction()

    companion object {
        internal var selfUserId: Long = 0
    }
}
