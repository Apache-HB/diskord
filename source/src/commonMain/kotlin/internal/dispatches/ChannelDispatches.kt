package com.serebit.diskord.internal.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.events.ChannelCreateEvent
import com.serebit.diskord.events.ChannelDeleteEvent
import com.serebit.diskord.events.ChannelPinsUpdateEvent
import com.serebit.diskord.events.ChannelUpdateEvent
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.TypingStartEvent
import com.serebit.diskord.internal.DispatchPayload
import com.serebit.diskord.internal.packets.GenericChannelPacket
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
    data class Data(val channel_id: Long, val last_pin_timestamp: IsoTimestamp?)
}

@Serializable
internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, d)

    @Serializable
    data class Data(val channel_id: Long, val user_id: Long, val timestamp: UnixTimestamp)
}
