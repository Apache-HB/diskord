package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.entities.channels.toChannel
import com.serebit.diskord.entities.channels.toTextChannel
import com.serebit.diskord.findChannelInCaches
import com.serebit.diskord.findTextChannelInCaches
import com.serebit.diskord.internal.caching.plusAssign
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GroupDmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildChannelData
import com.serebit.diskord.internal.entitydata.channels.toData
import com.serebit.diskord.internal.entitydata.channels.update
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> context.guildCache[it.guildId!!]?.allChannels?.put(it.id, it)
            is DmChannelData -> context.dmChannelCache += it
            is GroupDmChannelData -> context.groupDmChannelCache += it
        }
    }.toChannel()
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = packet.toTypedPacket().let { context.findChannelInCaches(it.id)!!.update(it) }.toChannel()
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channelId = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> context.guildCache.removeChannel(it.id)
            is DmChannelData -> context.dmChannelCache -= it.id
            is GroupDmChannelData -> context.groupDmChannelCache -= it.id
        }
    }.id
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = context.findTextChannelInCaches(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.toDateTime()
    }.toTextChannel()
}
