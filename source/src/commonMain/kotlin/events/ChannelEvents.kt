package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = Channel.from(packet.toTypedPacket().cache())
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = Channel.from(packet.toTypedPacket().cache())
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = Channel.from(packet.toTypedPacket().cache())
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = Channel.find(data.channel_id)
}
