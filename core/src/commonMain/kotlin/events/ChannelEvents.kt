package com.serebit.strife.events

import com.serebit.strife.Context
import com.serebit.strife.entities.Channel
import com.serebit.strife.entities.Message
import com.serebit.strife.entities.TextChannel
import com.serebit.strife.entities.User
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.dispatches.ChannelPinsUpdate
import com.serebit.strife.internal.dispatches.TypingStart
import com.serebit.strife.internal.entitydata.DmChannelData
import com.serebit.strife.internal.entitydata.GuildChannelData
import com.serebit.strife.internal.entitydata.toData
import com.serebit.strife.internal.packets.ChannelPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse

/** An [Event] based around a [Channel]. */
interface ChannelEvent : Event {
    val channel: Channel
}

/** Received when a new [Channel] is created. */
class ChannelCreateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    /** The created [Channel]. */
    override val channel = packet.toData(context).also {
        when (it) {
            is GuildChannelData<*, *> -> it.guild.allChannels[it.id] = it
            is DmChannelData -> context.dmCache[it.id] = it
        }
    }.toEntity()
}

/** Received when a [Channel] is updated. TODO More specific docs. */
class ChannelUpdateEvent internal constructor(override val context: Context, packet: ChannelPacket) : ChannelEvent {
    /** The updated [Channel] */
    override val channel = context.getChannelData(packet.id)!!.toEntity()
}

/** Received when a [Channel] is deleted. */
class ChannelDeleteEvent internal constructor(override val context: Context, packet: ChannelPacket) : Event {
    /** The [id][Channel.id] of the deleted [Channel]. */
    val channelID = packet.toData(context).also {
        when (it) {
            is GuildChannelData -> it.guild.allChannels -= it.id
            is DmChannelData -> context.dmCache -= it.id
        }
    }.id
}

/** Received when a [Message] is (un)pinned in a [TextChannel]. */
class ChannelPinsUpdateEvent internal constructor(
    override val context: Context, data: ChannelPinsUpdate.Data
) : ChannelEvent {
    /** The [TextChannel] with its [TextChannel.lastPinTime] updated. */
    override val channel = context.getTextChannelData(data.channel_id)!!.also {
        it.lastPinTime = data.last_pin_timestamp?.let { time -> DateFormat.ISO_WITH_MS.parse(time) }
    }.toEntity()
}

/** Received when a [User] starts typing in a [TextChannel]. */
class TypingStartEvent internal constructor(override val context: Context, data: TypingStart.Data) : ChannelEvent {
    /** The [User] who is typing. */
    val user by lazy { context.userCache[data.user_id]!!.toEntity() }
    /** The [TextChannel] in which the [user] is typing. */
    override val channel by lazy { context.getTextChannelData(data.channel_id)!!.toEntity() }
    /** The [DateTime] of the [TypingStartEvent]. */
    val timestamp = DateTime(data.timestamp)
}
