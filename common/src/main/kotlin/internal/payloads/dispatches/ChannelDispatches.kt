package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.IsoTimestamp
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.events.*
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.payloads.DispatchPayload

internal class ChannelCreate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelCreateEvent(context, d)
}

internal class ChannelUpdate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelUpdateEvent(context, d)
}

internal class ChannelDelete(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelDeleteEvent(context, d)
}

internal class ChannelPinsUpdate(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelPinsUpdateEvent(context, d)

    data class Data(val channel_id: Long, val last_pin_timestamp: IsoTimestamp?)
}

internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, this)

    data class Data(val channel_id: Long, val user_id: Long, val timestamp: UnixTimestamp)
}
