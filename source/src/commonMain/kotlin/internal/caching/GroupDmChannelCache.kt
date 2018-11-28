package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData

internal class GroupDmChannelCache : MutableMap<Long, GroupDmChannelData> by mutableMapOf()
