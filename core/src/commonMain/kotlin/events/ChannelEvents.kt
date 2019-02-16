package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.toChannel
import com.serebit.strife.entities.toTextChannel
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.*
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

interface ChannelEvent : Event {
    val channel: Channel
}

class ChannelCreateEvent internal constructor(
    override val context: Context, packet: GenericChannelPacket
) : ChannelEvent {
    override val channel = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels[it.id] = it
            is DmChannelData -> context.dmCache += (it.id to it)
            is GroupDmChannelData -> context.groupDmCache += (it.id to it)
        }
    }.toChannel()
}

class ChannelUpdateEvent internal constructor(
    override val context: Context, packet: GenericChannelPacket
) : ChannelEvent {
    override val channel = packet.toTypedPacket().let { context.getChannelData(it.id)!!.update(it) }.toChannel()
}

class ChannelDeleteEvent internal constructor(
    override val context: Context, packet: GenericChannelPacket
) : Event {
    val channelID = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels -= it.id
            is DmChannelData -> context.dmCache -= it.id
            is GroupDmChannelData -> context.groupDmCache -= it.id
        }
    }.id
}

class ChannelPinsUpdateEvent internal constructor(
    override val context: Context, data: ChannelPinsUpdate.Data
) : ChannelEvent {
    override val channel = context.getTextChannelData(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.let { time -> DateFormat.ISO_FORMAT.parse(time) }
    }.toTextChannel()
}

class TypingStartEvent internal constructor(
    override val context: Context, data: TypingStart.Data
) : ChannelEvent {
    val user by lazy { context.userCache[data.user_id]!!.toUser() }
    override val channel by lazy { context.getTextChannelData(data.channel_id)!!.toTextChannel() }
    val timestamp = DateTime(data.timestamp)
}
