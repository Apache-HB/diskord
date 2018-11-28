package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.channels.DmChannelData

internal class DmChannelCache : MutableMap<Long, DmChannelData> by mutableMapOf()
