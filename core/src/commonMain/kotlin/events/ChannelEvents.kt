package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Channel
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.DmChannelData
import com.serebit.strife.internal.entitydata.GuildChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.ChannelPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

interface ChannelEvent : Event {
    val channel: Channel
}

class ChannelCreateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    override val channel = packet.toData(context).also {
        when (it) {
            is GuildChannelData<*, *> -> it.guild.allChannels[it.id] = it
            is DmChannelData -> context.dmCache[it.id] = it
        }
    }.toEntity()
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    override val channel = context.getChannelData(packet.id)!!.toEntity()
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    val channelID = packet.toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels -= it.id
            is DmChannelData -> context.dmCache -= it.id
        }
    }.id
}

class ChannelPinsUpdateEvent internal constructor(
    override val context: Context, data: ChannelPinsUpdate.Data
) : ChannelEvent {
    override val channel = context.getTextChannelData(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.let { time -> DateFormat.ISO_FORMAT.parse(time) }
    }.toEntity()
}

class TypingStartEvent internal constructor(
    override val context: Context, data: TypingStart.Data
) : ChannelEvent {
    val user by lazy { context.userCache[data.user_id]!!.toEntity() }
    override val channel by lazy { context.getTextChannelData(data.channel_id)!!.toEntity() }
    val timestamp = DateTime(data.timestamp)
}
