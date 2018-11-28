package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.UserData
import com.serebit.diskord.internal.packets.EntityPacket
import com.serebit.diskord.internal.packets.UserPacket

internal class UserCache : EntityDataCache<UserData> {
    private val cache = mutableMapOf<Long, UserData>()

    override fun put(data: UserData) {
        cache[data.id] = data
    }

    override fun update(packet: EntityPacket) {
        cache[packet.id]?.update(packet as UserPacket)
    }

    override fun remove(id: Long) {
        cache -= id
    }

    override fun get(id: Long) = cache[id]
}
