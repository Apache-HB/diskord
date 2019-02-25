package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.data.UnknownEntityTypeException
import com.serebit.strife.entities.Channel
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.DmChannelData
import com.serebit.strife.internal.entitydata.GuildTextChannelData
import com.serebit.strife.internal.entitydata.GuildVoiceChannelData
import com.serebit.strife.internal.packets.ChannelPacket
import com.serebit.strife.internal.packets.DmChannelPacket
import com.serebit.strife.internal.packets.GuildTextChannelPacket
import com.serebit.strife.internal.packets.GuildVoiceChannelPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

interface ChannelEvent : Event {
    val channel: Channel
}

class ChannelCreateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    override val channel = context.cache.pushChannelData(packet).toEntity()
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    override val channel = when (packet) {
        is DmChannelPacket -> context.cache.getChannelDataAs<DmChannelData>(packet.id)?.apply{ update(packet) }
        is GuildVoiceChannelPacket -> {
            context.cache.getChannelDataAs<GuildVoiceChannelData>(packet.id)?.apply { update(packet) }
        }
        is GuildTextChannelPacket -> {
            context.cache.getChannelDataAs<GuildTextChannelData>(packet.id)?.apply { update(packet) }
        }
        else -> throw UnknownEntityTypeException(packet.toString())
    }!!.toEntity()
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channelID = packet.apply { context.cache.decache(id) }.id
}

class ChannelPinsUpdateEvent internal constructor(
    override val context: Context, data: ChannelPinsUpdate.Data
) : ChannelEvent {
    override val channel = context.cache.getTextChannelData(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.let { time -> DateFormat.ISO_WITH_MS.parse(time) }
    }.toEntity()
}

class TypingStartEvent internal constructor(
    override val context: Context, data: TypingStart.Data
) : ChannelEvent {
    val user by lazy { context.cache.getUserData(data.user_id)!!.toEntity() }
    override val channel by lazy { context.cache.getTextChannelData(data.channel_id)!!.toEntity() }
    val timestamp = DateTime(data.timestamp)
}
