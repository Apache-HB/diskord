package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.packets.ChannelPacket

class ChannelCreatedEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channel = Channel.from(packet)
}
