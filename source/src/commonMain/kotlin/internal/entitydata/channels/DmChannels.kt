package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.internal.entitydata.MessageData
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket

internal class DmChannelData(packet: DmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableListOf<MessageData>()
    override val lastMessage get() = messages.lastOrNull()
    var recipients = packet.recipients.map {
        context.cache.findUser(it.id) ?: context.cache.cache(packet.toData(context))
    }

    fun update(packet: DmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull { context.cache.findUser(it.id) }
    }
}

internal fun DmChannelPacket.toData(context: Context) = DmChannelData(this, context)

internal class GroupDmChannelData(packet: GroupDmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableListOf<MessageData>()
    override val lastMessage get() = messages.lastOrNull()
    var recipients = packet.recipients.map {
        context.cache.findUser(it.id) ?: context.cache.cache(packet.toData(context))
    }
    var owner = context.cache.findUser(packet.owner_id)
    var name = packet.name
    var iconHash = packet.icon

    fun update(packet: GroupDmChannelPacket) = apply {
        recipients = packet.recipients.mapNotNull { context.cache.findUser(it.id) }
        owner = context.cache.findUser(packet.owner_id)!!
        name = packet.name
        iconHash = packet.icon
    }
}

internal fun GroupDmChannelPacket.toData(context: Context) = GroupDmChannelData(this, context)
