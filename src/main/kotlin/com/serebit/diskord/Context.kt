package com.serebit.diskord

import com.serebit.diskord.entities.User

class Context internal constructor(private val selfUserId: Long) {
    val selfUser get() = EntityCache.find<User>(selfUserId)
}
