package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GroupDmChannelPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.TextChannelPacket
import com.serebit.strife.time.DateTime

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
