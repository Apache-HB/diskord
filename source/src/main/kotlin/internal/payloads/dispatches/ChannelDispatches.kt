package com.serebit.diskord.internal.payloads.dispatches

import com.serebit.diskord.Context
import com.serebit.diskord.Snowflake
import com.serebit.diskord.UnixTimestamp
import com.serebit.diskord.events.ChannelCreatedEvent
import com.serebit.diskord.events.Event
import com.serebit.diskord.events.TypingStartEvent
import com.serebit.diskord.internal.packets.ChannelPacket
import com.serebit.diskord.internal.payloads.DispatchPayload

internal class ChannelCreate(override val s: Int, override val d: ChannelPacket) : DispatchPayload() {
    override suspend fun asEvent(context: Context) = ChannelCreatedEvent(context, d)
}

internal class TypingStart(override val s: Int, override val d: Data) : DispatchPayload() {
    override suspend fun asEvent(context: Context): Event? = TypingStartEvent(context, this)

    data class Data(val channel_id: Snowflake, val user_id: Snowflake, val timestamp: UnixTimestamp)
}
