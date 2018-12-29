package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.time.toDateTime
import com.serebit.strife.internal.caching.plusAssign
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GroupDmChannelPacket

internal class DmChannelData(packet: DmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableMapOf<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var recipients = packet.recipients.map { recipient ->
        context.userCache[recipient.id] ?: recipient.toData(context).also { context.userCache += it }
    }

    fun update(packet: DmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull { context.userCache[it.id] }
    }
}

internal class GroupDmChannelData(packet: GroupDmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableMapOf<Long, MessageData>()
    override val lastMessage get() = messages.values.maxBy { it.createdAt }
    var recipients = packet.recipients.map { recipient ->
        context.userCache[recipient.id] ?: recipient.toData(context).also { context.userCache += it }
    }
    var owner = context.userCache[packet.owner_id]!!
    var name = packet.name
    var iconHash = packet.icon

    fun update(packet: GroupDmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull { context.userCache[it.id] }
        owner = context.userCache[packet.owner_id]!!
        name = packet.name
        iconHash = packet.icon
    }
}

internal fun DmChannelPacket.toDmChannelData(context: Context) = DmChannelData(this, context)

internal fun GroupDmChannelPacket.toGroupDmChannelData(context: Context) = GroupDmChannelData(this, context)
