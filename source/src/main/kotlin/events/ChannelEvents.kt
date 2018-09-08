package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channel = Channel.from(packet).cache()
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channel = Channel.from(packet).cache()
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channel = Channel.from(packet).cache()
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, packet: ChannelPinsUpdate.Data) :
    Event {
    val channel = Channel.find(packet.channel_id)
}
