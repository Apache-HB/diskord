package com.serebit.diskord.entities.channels

import com.serebit.diskord.EntityCache
import com.serebit.diskord.entities.User
import com.serebit.diskord.packets.ChannelPacket
import com.serebit.diskord.packets.GroupDmChannelPacket
import com.serebit.diskord.packets.TextChannelPacket

class GroupDmChannel internal constructor(packet: GroupDmChannelPacket) : TextChannel {
    override val id = packet.id
    var name: String = packet.name
        private set
    var recipients = packet.recipients.map { User(it) }
        private set
    var owner = recipients.first { it.id == packet.owner_id }
        private set

    internal constructor(packet: TextChannelPacket) : this(
        GroupDmChannelPacket(
            packet.id, packet.type, packet.owner_id!!, packet.name!!, packet.icon!!, packet.recipients!!,
            packet.last_message_id
        )
    )

    internal constructor(packet: ChannelPacket) : this(
        GroupDmChannelPacket(
            packet.id, packet.type, packet.owner_id!!, packet.name!!, packet.icon!!, packet.recipients!!,
            packet.last_message_id
        )
    )

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 3
    }
}
