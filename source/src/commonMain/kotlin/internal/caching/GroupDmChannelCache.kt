package com.serebit.strife.internal.caching

import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData

internal class GroupDmChannelCache : MutableMap<Long, GroupDmChannelData> by mutableMapOf()
