package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.UserData

internal class UserCache : MutableMap<Long, UserData> by mutableMapOf()
