package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.User
import com.serebit.diskord.packets.GroupDmChannelPacket

class GroupDmChannel internal constructor(packet: GroupDmChannelPacket) : TextChannel {
    override val id = packet.id
    var name: String = packet.name
        private set
    var recipients = packet.recipients.map { User(it) }
        private set
    var owner = recipients.first { it.id == packet.owner_id }
        private set

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 3
    }
}
