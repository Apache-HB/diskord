package com.serebit.diskord.entities.channels

import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityCache
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.TextChannelPacket

class DmChannel internal constructor(packet: DmChannelPacket) : TextChannel {
    override val id = packet.id
    val lastMessage: Message? = packet.last_message_id?.let { EntityCache.find(id) }
    var recipients: List<User> = packet.recipients.map { User(it) }
        private set

    internal constructor(packet: TextChannelPacket) : this(
        DmChannelPacket(packet.id, packet.type, packet.last_message_id, packet.recipients!!)
    )

    internal constructor(packet: ChannelPacket) : this(
        DmChannelPacket(packet.id, packet.type, packet.last_message_id, packet.recipients!!)
    )

    init {
        EntityCache.cache(this)
    }

    companion object {
        internal const val typeCode = 1

        fun find(id: Long) = Channel.find(id) as? DmChannel
    }
}


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

        fun find(id: Long) = Channel.find(id) as? GroupDmChannel
    }
}
