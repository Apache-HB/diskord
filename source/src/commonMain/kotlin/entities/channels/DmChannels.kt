package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetDmChannel
import com.serebit.diskord.internal.network.endpoints.GetGroupDmChannel
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket

class DmChannel internal constructor(override val id: Long) : TextChannel {
    private val packet: DmChannelPacket
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetDmChannel(id))
            ?: throw EntityNotFoundException("Invalid DM channel instantiated with ID $id.")
    val lastMessage: Message? get() = packet.lastMessage
    val recipients: List<User> get() = packet.recipientUsers

    companion object {
        internal const val typeCode = 1
    }
}


class GroupDmChannel internal constructor(override val id: Long) : TextChannel {
    private val packet: GroupDmChannelPacket
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetGroupDmChannel(id))
            ?: throw EntityNotFoundException("Invalid group DM channel instantiated with ID $id.")
    val name: String get() = packet.name
    val recipients get() = packet.recipientUsers
    val owner get() = packet.owner

    companion object {
        internal const val typeCode = 3
    }
}
