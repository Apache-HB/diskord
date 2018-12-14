package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.Context
import com.serebit.diskord.internal.entitydata.MessageData
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.TextChannelPacket
import com.serebit.diskord.time.DateTime

internal interface TextChannelData : ChannelData {
    val lastMessage: MessageData?
    var lastPinTime: DateTime?
    val messages: MutableMap<Long, MessageData>
}

internal fun TextChannelPacket.toData(context: Context) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GroupDmChannelPacket -> toGroupDmChannelData(context)
    is GuildTextChannelPacket -> toGuildTextChannelData(context)
    else -> throw IllegalStateException("Attempted to convert an unknown TextChannelPacket type to TextChannelData.")
}
