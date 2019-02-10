package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.channels.toChannel
import com.serebit.strife.entities.channels.toTextChannel
import com.serebit.strife.entities.toUser
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.channels.DmChannelData
import com.serebit.strife.internal.entitydata.channels.GroupDmChannelData
import com.serebit.strife.internal.entitydata.channels.GuildChannelData
import com.serebit.strife.internal.entitydata.channels.toData
import com.serebit.strife.internal.entitydata.channels.update
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels[it.id] = it
            is DmChannelData -> context.dmCache + (it.id to it)
            is GroupDmChannelData -> context.groupDmCache + (it.id to it)
        }
    }.toChannel()
}

class ChannelUpdateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = packet.toTypedPacket().let {
        context.getChannelData(it.id)!!.update(it)
    }.toChannel()
}

class ChannelDeleteEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channelId = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels - it.id
            is DmChannelData -> context.dmCache - it.id
            is GroupDmChannelData -> context.groupDmCache - it.id
        }
    }.id
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = context.getTextChannelData(data.channel_id)!!.also { cd ->
        cd.lastPinTime = data.last_pin_timestamp?.let {
            DateFormat.ISO_FORMAT.parse(it)
        }
    }.toTextChannel()
}

class TypingStartEvent internal constructor(override val context: Context, data: TypingStart.Data) : Event {
    val user by lazy { context.userCache[data.user_id]!!.toUser() }
    val channel by lazy {
        context.getTextChannelData(data.channel_id)!!.toTextChannel()
    }
    val timestamp = DateTime(data.timestamp)
}
