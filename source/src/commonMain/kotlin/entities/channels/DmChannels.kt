package com.serebit.diskord.entities.channels

import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.network.Requester
import com.serebit.diskord.internal.network.endpoints.GetDmChannel
import com.serebit.diskord.internal.network.endpoints.GetGroupDmChannel

class DmChannel internal constructor(override val id: Long) : TextChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetDmChannel(id))
            ?: throw EntityNotFoundException("Invalid DM channel instantiated with ID $id.")
    override val lastMessage get() = packet.lastMessage
    override val lastPinTime get() = packet.lastPinTime
    val recipients get() = packet.recipientUsers

    companion object {
        internal const val typeCode = 1
    }
}


class GroupDmChannel internal constructor(override val id: Long) : TextChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: Requester.requestObject(GetGroupDmChannel(id))
            ?: throw EntityNotFoundException("Invalid group DM channel instantiated with ID $id.")
    override val lastMessage get() = packet.lastMessage
    override val lastPinTime get() = packet.lastPinTime
    val name get() = packet.name
    val recipients get() = packet.recipientUsers
    val owner get() = packet.owner

    companion object {
        internal const val typeCode = 3
    }
}
