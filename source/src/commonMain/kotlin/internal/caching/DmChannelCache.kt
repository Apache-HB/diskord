package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.EntityPacket

internal class DmChannelCache : EntityDataCache<DmChannelData> {
    private val cache = mutableMapOf<Long, DmChannelData>()

    override fun put(data: DmChannelData) {
        cache[data.id] = data
    }

    override fun update(packet: EntityPacket) {
        cache[packet.id]?.update(packet as DmChannelPacket)
    }

    override fun remove(id: Long) {
        cache -= id
    }

    override fun get(id: Long) = cache[id]
}
