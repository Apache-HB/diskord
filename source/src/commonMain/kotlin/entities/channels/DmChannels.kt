package com.serebit.diskord.entities.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.EntityNotFoundException
import com.serebit.diskord.entities.Message
import com.serebit.diskord.entities.User
import com.serebit.diskord.internal.EntityPacketCache
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.network.endpoints.GetDmChannel
import com.serebit.diskord.internal.network.endpoints.GetGroupDmChannel

class DmChannel internal constructor(override val id: Long, override val context: Context) : TextChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: context.requester.requestObject(GetDmChannel(id))
            ?: throw EntityNotFoundException("Invalid DM channel instantiated with ID $id.")
    override val lastMessage get() = packet.last_message_id?.let { Message(it, packet.id, context) }
    override val lastPinTime get() = packet.lastPinTime
    val recipients get() = packet.recipients.map { User(it.id, context) }

    companion object {
        internal const val typeCode = 1
    }
}

internal fun DmChannelData.toDmChannel() = DmChannel(id, context)

class GroupDmChannel internal constructor(override val id: Long, override val context: Context) : TextChannel {
    private val packet
        get() = EntityPacketCache.findId(id)
            ?: context.requester.requestObject(GetGroupDmChannel(id))
            ?: throw EntityNotFoundException("Invalid group DM channel instantiated with ID $id.")
    override val lastMessage get() = packet.last_message_id?.let { Message(it, packet.id, context) }
    override val lastPinTime get() = packet.lastPinTime
    val name get() = packet.name
    val recipients get() = packet.recipients.map { User(it.id, context) }
    val owner get() = User(packet.owner_id, context)

    companion object {
        internal const val typeCode = 3
    }
}

internal fun GroupDmChannelData.toGroupDmChannel() = GroupDmChannel(id, context)
