package com.serebit.strife.internal.caching

import com.serebit.strife.internal.entitydata.UserData

internal class UserCache : MutableMap<Long, UserData> by mutableMapOf()
