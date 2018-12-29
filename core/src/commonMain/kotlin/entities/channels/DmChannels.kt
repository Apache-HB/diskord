package com.serebit.strife.entities.channels

import com.serebit.strife.entities.toMessage
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.entitydata.channels.DmChannelData
import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData

class DmChannel internal constructor(private val data: DmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val recipients get() = data.recipients.map { it.toUser() }

    companion object {
        internal const val typeCode = 1
    }
}

class GroupDmChannel internal constructor(private val data: GroupDmChannelData) : TextChannel {
    override val id = data.id
    override val context = data.context
    override val lastMessage get() = data.lastMessage?.toMessage()
    override val lastPinTime get() = data.lastPinTime
    val name get() = data.name
    val recipients get() = data.recipients.map { it.toUser() }
    val owner get() = data.owner.toUser()

    companion object {
        internal const val typeCode = 3
    }
}

internal fun DmChannelData.toDmChannel() = DmChannel(this)

internal fun GroupDmChannelData.toGroupDmChannel() = GroupDmChannel(this)
