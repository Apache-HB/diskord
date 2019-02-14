package com.serebit.strife.internal.dispatches

import com.serebit.strife.Context
import com.serebit.strife.events.ChannelCreateEvent
import com.serebit.strife.events.ChannelDeleteEvent
import com.serebit.strife.events.ChannelPinsUpdateEvent
import com.serebit.strife.events.ChannelUpdateEvent
import com.serebit.strife.events.Event
import com.serebit.strife.events.TypingStartEvent
import com.serebit.strife.internal.DispatchPayload
import com.serebit.strife.internal.packets.GenericChannelPacket
import kotlinx.serialization.Serializable

@Serializable
internal class ChannelCreate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelCreateEvent(context, d)
}

@Serializable
internal class ChannelUpdate(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelUpdateEvent(context, d)
}

@Serializable
internal class ChannelDelete(override val s: Int, override val d: GenericChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelDeleteEvent(context, d)
}

@Serializable
internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelPinsUpdateEvent(context, d)

    @Serializable
    data class Data(val channel_id: Long, val last_pin_timestamp: String?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, d)

    @Serializable
    data class Data(val channel_id: Long, val user_id: Long, val timestamp: Long)
}
