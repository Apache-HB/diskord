package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.cacheAll
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetDmChannel
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.TextChannelPacket

class DmChannel internal constructor(override val id: Long) : TextChannel {
    private val packet: DmChannelPacket
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetDmChannel(id))
            ?: throw EntityNotFoundException("Invalid DM channel instantiated with ID $id.")
    val lastMessage: Message? get() = packet.last_message_id?.let { Message(it, id) }
    val recipients: List<User> get() = packet.recipients.map { User(it.id) }

    companion object {
        internal const val typeCode = 1
    }
}


class GroupDmChannel internal constructor(packet: GroupDmChannelPacket) : TextChannel {
    override val id = packet.id
    val name: String = packet.name
    val recipients = packet.recipients.cacheAll().map { User(it.id) }
    val owner = recipients.first { it.id == packet.owner_id }

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

    companion object {
        internal const val typeCode = 3

        fun find(id: Long) = Channel.find(id) as? GroupDmChannel
    }
}
