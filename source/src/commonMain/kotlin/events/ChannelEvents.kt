package com.serebit.diskord.events

import com.serebit.diskord.Context
import com.serebit.diskord.data.toDateTime
import com.serebit.diskord.entities.channels.Channel
import com.serebit.diskord.internal.cache
import com.serebit.diskord.internal.entitydata.channels.ChannelCategoryData
import com.serebit.diskord.internal.entitydata.channels.DmChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildTextChannelData
import com.serebit.diskord.internal.entitydata.channels.GuildVoiceChannelData
import com.serebit.diskord.internal.entitydata.channels.TextChannelData
import com.serebit.diskord.internal.entitydata.findById
import com.serebit.diskord.internal.entitydata.removeById
import com.serebit.diskord.internal.packets.ChannelCategoryPacket
import com.serebit.diskord.internal.packets.DmChannelPacket
import com.serebit.diskord.internal.packets.GenericChannelPacket
import com.serebit.diskord.internal.packets.GuildChannelPacket
import com.serebit.diskord.internal.packets.GuildTextChannelPacket
import com.serebit.diskord.internal.packets.GuildVoiceChannelPacket
import com.serebit.diskord.internal.payloads.dispatches.ChannelPinsUpdate

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        when (typedPacket) {
            is DmChannelPacket -> context.cache.dmChannels[packet.id] = DmChannelData(typedPacket, context)
        }
    }
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        when (typedPacket) {
            is DmChannelPacket -> context.cache.dmChannels[packet.id]!!.update(typedPacket)
            is GuildChannelPacket -> {
                val data = context.cache.guilds[packet.guild_id]!!.allChannels.findById(packet.id)!!
                when (data) {
                    is GuildTextChannelData -> data.update(typedPacket as GuildTextChannelPacket)
                    is GuildVoiceChannelData -> data.update(typedPacket as GuildVoiceChannelPacket)
                    is ChannelCategoryData -> data.update(typedPacket as ChannelCategoryPacket)
                }
            }
        }
    }
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    private val typedPacket = packet.toTypedPacket()
    val channel = Channel.from(typedPacket.cache(), context)

    init {
        when (typedPacket) {
            is DmChannelPacket -> context.cache.dmChannels -= packet.id
            is GuildChannelPacket -> context.cache.guilds[packet.guild_id]!!.allChannels.removeById(packet.id)
        }
    }
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = Channel.find(data.channel_id, context)

    init {
        val channelData = context.cache.findChannel<TextChannelData>(data.channel_id)
        println(channelData?.lastPinTime)
        channelData?.lastPinTime = data.last_pin_timestamp?.toDateTime()
        println(channelData?.lastPinTime)
    }
}
