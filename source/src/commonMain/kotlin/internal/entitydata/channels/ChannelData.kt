package com.serebit.diskord.internal.entitydata.channels

import com.serebit.diskord.internal.entitydata.EntityData
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket

internal interface ChannelData : EntityData {
    val type: Int
}

internal fun ChannelData.update(packet: ChannelPacket) = when (this) {
    is DmChannelData -> update(packet as DmChannelPacket)
    is GroupDmChannelData -> update(packet as GroupDmChannelPacket)
    is GuildChannelData -> update(packet as GuildChannelPacket)
    else -> throw IllegalStateException("Attempted to update an unknown ChannelData type.")
}
