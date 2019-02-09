package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.internal.entitydata.MessageData
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GroupDmChannelPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.TextChannelPacket
import com.soywiz.klock.DateTimeTz

internal interface TextChannelData : ChannelData {
    val lastMessage: MessageData?
    var lastPinTime: DateTimeTz?
    val messages: MutableMap<Long, MessageData>
}

internal fun TextChannelPacket.toData(context: Context) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GroupDmChannelPacket -> toGroupDmChannelData(context)
    is GuildTextChannelPacket -> toGuildTextChannelData(context.guildCache[guild_id!!]!!, context)
    else -> throw IllegalStateException("Attempted to convert an unknown TextChannelPacket type to TextChannelData.")
}
