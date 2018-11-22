package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.internal.entitydata.MessageData
import com.serebit.diskord.internal.entitydata.UserData
import com.serebit.diskord.internal.packets.DmChannelPacket

internal class DmChannelData(packet: DmChannelPacket, override val context: Context) : TextChannelData {
    override val id = packet.id
    override val type = packet.type
    var recipients = packet.recipients.map {
        context.cache.users[it.id] ?: context.cache.users.put(it.id, UserData(it, context))
    }
    override var lastMessageId = packet.last_message_id
    override var lastPinTime = packet.last_pin_timestamp?.toDateTime()
    override val messages = mutableListOf<MessageData>()

    fun update(packet: DmChannelPacket) = apply {
        recipients = packet.recipients.map {
            context.cache.users[it.id] ?: context.cache.users.put(it.id, UserData(it, context))
        }
    }
}
