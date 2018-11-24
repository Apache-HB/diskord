package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        context.cache.cache(typedPacket.toData(context))
    }
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        context.cache.update(typedPacket)
    }
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        context.cache.removeChannel(packet.id)
    }
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = Channel.find(data.channel_id, context)

    init {
        context.cache.findChannel<TextChannelData>(data.channel_id)?.lastPinTime = data.last_pin_timestamp?.toDateTime()
    }
}
