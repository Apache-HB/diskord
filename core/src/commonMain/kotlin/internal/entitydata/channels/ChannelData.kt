package com.serebit.strife.internal.entitydata.channels

import com.serebit.strife.Context
import com.serebit.strife.internal.entitydata.EntityData
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GroupDmChannelPacket
import com.serebit.strife.internal.packets.GuildChannelPacket

internal interface ChannelData : EntityData {
    val type: Int
}

internal fun ChannelData.update(packet: ChannelPacket) = when (this) {
    is DmChannelData -> update(packet as DmChannelPacket)
    is GroupDmChannelData -> update(packet as GroupDmChannelPacket)
    is GuildChannelData -> update(packet as GuildChannelPacket)
    else -> throw IllegalStateException("Attempted to update an unknown ChannelData type.")
}

internal fun ChannelPacket.toData(context: Context) = when (this) {
    is DmChannelPacket -> toDmChannelData(context)
    is GroupDmChannelPacket -> toGroupDmChannelData(context)
    is GuildChannelPacket -> {
        toGuildChannelData(context.guildCache[guild_id!!]!!, context)
    }
    else -> throw IllegalStateException("Attempted to convert an unknown ChannelPacket type to ChannelData.")
}
