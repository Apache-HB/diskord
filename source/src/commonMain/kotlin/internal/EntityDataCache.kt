package com.serebit.diskord.internal

import com.serebit.diskord.internal.entitydata.GuildData
import com.serebit.diskord.internal.entitydata.UserData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData

internal class EntityDataCache {
    internal val guilds = mutableMapOf<Long, GuildData>()
    internal val dmChannels = mutableMapOf<Long, DmChannelData>()
    internal val users = mutableMapOf<Long, UserData>()
}
