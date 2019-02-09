package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.channels.toChannel
import com.serebit.strife.entities.channels.toTextChannel
import com.serebit.strife.entities.toUser
import com.serebit.strife.findChannelInCaches
import com.serebit.strife.findTextChannelInCaches
import com.serebit.strife.internal.ISO_FORMAT
import com.serebit.strife.internal.caching.plusAssign
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.channels.*
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.runBlocking
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

class ChannelCreateEvent internal constructor(override val context: Context, packet: GenericChannelPacket) : Event {
    val channel = packet.toTypedPacket().toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels[it.id] = it
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
            is GuildChannelData -> runBlocking { context.guildCache.removeChannel(it.id) }
            is DmChannelData -> context.dmChannelCache -= it.id
            is GroupDmChannelData -> context.groupDmChannelCache -= it.id
        }
    }.id
}

class ChannelPinsUpdateEvent internal constructor(override val context: Context, data: ChannelPinsUpdate.Data) : Event {
    val channel = context.findTextChannelInCaches(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.let { time -> DateFormat.ISO_FORMAT.parse(time) }
    }.toTextChannel()
}

class TypingStartEvent internal constructor(override val context: Context, data: TypingStart.Data) : Event {
    val user by lazy { context.userCache[data.user_id]!!.toUser() }
    val channel by lazy { context.findTextChannelInCaches(data.channel_id)!!.toTextChannel() }
    val timestamp = DateTime(data.timestamp)
}
