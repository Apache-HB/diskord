package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.*
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.ISO_WITH_MS
import com.serebit.strife.internal.packets.GenericChannelPacket
import com.serebit.strife.internal.packets.toTypedPacket
import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.parse
import kotlinx.serialization.Serializable

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) =
        ChannelCreateEvent(context, context.cache.pushChannelData(d.toTypedPacket()).toEntity())
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) =
        ChannelUpdateEvent(context, context.cache.pullChannelData(d.toTypedPacket()).toEntity())
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) =
        ChannelDeleteEvent(context, context.cache.pullChannelData(d.toTypedPacket()).toEntity(), d.id)
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): ChannelPinsUpdateEvent? {
        val channelData = context.cache.getTextChannelData(d.channel_id) ?: return null
        d.last_pin_timestamp?.let { channelData.lastPinTime = DateFormat.ISO_WITH_MS.parse(it) }

        return ChannelPinsUpdateEvent(context, channelData.toEntity())
    }

    @Serializable
    data class Data(val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): Event? {
        val channel = context.cache.getTextChannelData(d.channel_id)?.toEntity() ?: return null
        val user = context.cache.getUserData(d.user_id)?.toEntity() ?: return null

        return TypingStartEvent(context, channel, user, DateTime(d.timestamp))
    }

    @Serializable
    data class Data(val channel_id: Long, val user_id: Long, val timestamp: Long)
}
