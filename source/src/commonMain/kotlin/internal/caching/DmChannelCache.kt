package com.serebit.strife.internal.caching

import com.serebit.strife.internal.entitydata.channels.DmChannelData

internal class DmChannelCache : MutableMap<Long, DmChannelData> by mutableMapOf()
