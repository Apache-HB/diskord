package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.findChannelInCaches
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.caching.minusAssign
import com.serebit.diskord.internal.caching.plusAssign
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.entitydata.channels.update
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.packets.GroupDmChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        when (val data = typedPacket.toData(context)) {
            is GuildChannelData -> context.guildCache[data.guildId!!]?.allChannels?.put(data.id, data)
            is DmChannelData -> context.dmChannelCache += data
            is GroupDmChannelData -> context.groupDmChannelCache += data
        }
    }
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        context.findChannelInCaches(packet.id)?.update(typedPacket)
    }
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        when (typedPacket) {
            is GuildChannelPacket -> context.guildCache.removeChannel(packet.id)
            is DmChannelPacket -> context.dmChannelCache -= packet.id
            is GroupDmChannelPacket -> context.groupDmChannelCache -= packet.id
        }
    }
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = Channel.find(data.channel_id, context)

    init {
        (context.findChannelInCaches(data.channel_id) as? TextChannelData)
            ?.lastPinTime = data.last_pin_timestamp?.toDateTime()
    }
}
