package com.serebit.diskord.internal.caching

import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.packets.EntityPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket

internal class GroupDmChannelCache : EntityDataCache<GroupDmChannelData> {
    private val cache = mutableMapOf<Long, GroupDmChannelData>()

    override fun put(data: GroupDmChannelData) {
        cache[data.id] = data
    }

    override fun update(packet: EntityPacket) {
        cache[packet.id]?.update(packet as GroupDmChannelPacket)
    }

    override fun remove(id: Long) {
        cache -= id
    }

    override fun get(id: Long) = cache[id]
}
